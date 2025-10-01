package com.irada.blockerheroar.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.irada.blockerheroar.R
import com.irada.blockerheroar.data.BlockedContent
import com.irada.blockerheroar.data.ContentType
import com.irada.blockerheroar.utils.BlockedContentManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockedListScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var blockedContent by remember { mutableStateOf<List<BlockedContent>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(ContentType.KEYWORD) }
    var isLoading by remember { mutableStateOf(true) }
    
    // تحميل البيانات عند فتح الصفحة
    LaunchedEffect(Unit) {
        isLoading = true
        blockedContent = BlockedContentManager.getAllBlockedContent()
        isLoading = false
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.nav_blocked_list),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_content))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tabs
        TabRow(
            selectedTabIndex = when (selectedType) {
                ContentType.KEYWORD -> 0
                ContentType.WEBSITE -> 1
                ContentType.APP -> 2
            }
        ) {
            Tab(
                selected = selectedType == ContentType.KEYWORD,
                onClick = { selectedType = ContentType.KEYWORD },
                text = { Text(stringResource(R.string.blocked_keywords)) }
            )
            Tab(
                selected = selectedType == ContentType.WEBSITE,
                onClick = { selectedType = ContentType.WEBSITE },
                text = { Text(stringResource(R.string.blocked_websites)) }
            )
            Tab(
                selected = selectedType == ContentType.APP,
                onClick = { selectedType = ContentType.APP },
                text = { Text(stringResource(R.string.blocked_apps)) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Loading or Content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Content List
            val filteredContent = blockedContent.filter { it.type == selectedType }
            
            if (filteredContent.isEmpty()) {
                // Empty State
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Block,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(R.string.no_blocked_content),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredContent) { content ->
                        BlockedContentCard(
                            content = content,
                            onRemove = {
                                scope.launch {
                                    val success = BlockedContentManager.removeBlockedContent(content.id)
                                    if (success) {
                                        // تحديث القائمة المحلية
                                        blockedContent = blockedContent.filter { it.id != content.id }
                                    }
                                }
                            },
                            onSendPartnerRequest = {
                                // Send partner request logic
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add Content Dialog
    if (showAddDialog) {
        AddBlockedContentDialog(
            contentType = selectedType,
            onDismiss = { showAddDialog = false },
            onAdd = { content ->
                scope.launch {
                    val success = BlockedContentManager.addBlockedContent(content)
                    if (success) {
                        // تحديث القائمة المحلية
                        blockedContent = blockedContent + content
                    }
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BlockedContentCard(
    content: BlockedContent,
    onRemove: () -> Unit,
    onSendPartnerRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = content.content,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = when (content.type) {
                        ContentType.KEYWORD -> stringResource(R.string.content_type_keyword)
                        ContentType.WEBSITE -> stringResource(R.string.content_type_website)
                        ContentType.APP -> stringResource(R.string.content_type_app)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Remove Button
            IconButton(
                onClick = onRemove
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            
            // Partner Request Button
            IconButton(
                onClick = onSendPartnerRequest
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = stringResource(R.string.partner_request),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBlockedContentDialog(
    contentType: ContentType,
    onDismiss: () -> Unit,
    onAdd: (BlockedContent) -> Unit
) {
    var content by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when (contentType) {
                    ContentType.KEYWORD -> stringResource(R.string.add_keyword)
                    ContentType.WEBSITE -> stringResource(R.string.add_website)
                    ContentType.APP -> stringResource(R.string.add_app)
                }
            )
        },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = {
                    Text(
                        when (contentType) {
                            ContentType.KEYWORD -> stringResource(R.string.keyword_label)
                            ContentType.WEBSITE -> stringResource(R.string.website_label)
                            ContentType.APP -> stringResource(R.string.app_label)
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotEmpty()) {
                        val blockedContent = BlockedContent(
                            id = System.currentTimeMillis().toString(),
                            type = contentType,
                            content = content,
                            isActive = true
                        )
                        onAdd(blockedContent)
                    }
                },
                enabled = content.isNotEmpty()
            ) {
                Text(stringResource(R.string.add_content))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}