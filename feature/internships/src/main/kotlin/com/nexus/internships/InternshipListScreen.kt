package com.nexus.internships

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusChip
import com.nexus.domain.model.Internship
import com.nexus.domain.model.Location
import com.nexus.domain.model.Stipend
import com.nexus.domain.model.Company
import com.nexus.domain.model.Duration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InternshipListScreen(
    onInternshipClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilters by remember { mutableStateOf(setOf<String>()) }

    val filters = listOf("Remote", "Tech", "Paid", "AI/ML", "Web", "Mobile")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Internships", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search internships...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                singleLine = true
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    NexusChip(
                        text = filter,
                        isSelected = filter in selectedFilters,
                        onToggle = {
                            selectedFilters = if (filter in selectedFilters)
                                selectedFilters - filter
                            else selectedFilters + filter
                        }
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp, vertical = 8.dp
                )
            ) {
                items(sampleInternships) { internship ->
                    InternshipListItem(
                        internship = internship,
                        onClick = { onInternshipClick(internship.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InternshipListItem(
    internship: Internship,
    onClick: () -> Unit,
) {
    var isSaved by remember { mutableStateOf(internship.isSaved) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = internship.company.name.take(2).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = internship.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = internship.company.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = internship.location.city,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                internship.stipend?.let {
                    Text(
                        text = it.formatted(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        IconButton(onClick = { isSaved = !isSaved }) {
            Icon(
                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Save",
                tint = if (isSaved) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

val sampleInternships = listOf(
    Internship("1", "Android Developer Intern", Company("Google", domain = "Tech"), Location("Bangalore"), Stipend(50000), Duration(6), deadline = java.time.LocalDate.now().plusDays(15), applyUrl = "", postedAt = java.time.Instant.now(), tags = kotlinx.collections.immutable.persistentListOf("Kotlin", "Compose")),
    Internship("2", "Software Engineer Intern", Company("Microsoft", domain = "Tech"), Location("Hyderabad"), Stipend(45000), Duration(3), deadline = java.time.LocalDate.now().plusDays(21), applyUrl = "", postedAt = java.time.Instant.now(), tags = kotlinx.collections.immutable.persistentListOf("C++", "Azure")),
    Internship("3", "Data Science Intern", Company("Amazon", domain = "Tech"), Location("Remote", country = "India"), Stipend(40000), Duration(6), deadline = java.time.LocalDate.now().plusDays(10), applyUrl = "", postedAt = java.time.Instant.now(), isRemote = true, tags = kotlinx.collections.immutable.persistentListOf("Python", "ML")),
    Internship("4", "Frontend Developer Intern", Company("Flipkart", domain = "E-commerce"), Location("Bangalore"), Stipend(35000), Duration(6), deadline = java.time.LocalDate.now().plusDays(30), applyUrl = "", postedAt = java.time.Instant.now(), tags = kotlinx.collections.immutable.persistentListOf("React", "TypeScript")),
    Internship("5", "Backend Developer Intern", Company("Uber", domain = "Tech"), Location("Bangalore"), Stipend(55000), Duration(6), deadline = java.time.LocalDate.now().plusDays(7), applyUrl = "", postedAt = java.time.Instant.now(), tags = kotlinx.collections.immutable.persistentListOf("Go", "Kafka")),
    Internship("6", "ML Engineer Intern", Company("Tesla", domain = "Automotive"), Location("Remote", country = "India"), Stipend(60000), Duration(6), deadline = java.time.LocalDate.now().plusDays(45), applyUrl = "", postedAt = java.time.Instant.now(), isRemote = true, tags = kotlinx.collections.immutable.persistentListOf("Python", "TensorFlow")),
)
