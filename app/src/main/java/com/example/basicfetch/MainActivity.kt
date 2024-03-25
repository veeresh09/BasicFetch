package com.example.basicfetch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.basicfetch.models.Item
import com.example.basicfetch.network.ApiService
import com.example.basicfetch.network.retrofit
import com.example.basicfetch.ui.theme.BasicFetchTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicFetchTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ItemListScreen()
                }
            }
        }
    }
}

@Composable
fun ItemListScreen(apiService: ApiService = retrofit.create(ApiService::class.java)) {
    val items = remember { mutableStateListOf<Item>() }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        try {
            val fetchedItems = apiService.fetchItems()
            .filter { !it.name.isNullOrBlank() }
            .sortedWith(compareBy({ it.listId }, { it.name }))
            items.addAll(fetchedItems)
            errorMessage.value = null
        } catch (e: Exception) {
            errorMessage.value = e.message
        }

    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (errorMessage.value != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Something went wrong.", color = Color.Red)
                Text(text = "Please check your internet connection.", color = Color.Red)
                if (isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                } else {
                    Button(
                        onClick = { coroutineScope.launch { fetchData(apiService, items, errorMessage, isLoading) } },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(text = "Try Again")
                    }
                }
            }
        } else {
            Column {
                LazyColumn(state = scrollState) {
                    items(items) { item ->
                        ItemCard(item)
                    }
                }
            }
            if (scrollState.firstVisibleItemIndex > 0) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(0)
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Icon(imageVector = Icons.Filled.KeyboardArrowUp, contentDescription = "Scroll to top")
                }
            }
        }
    }

}

@Composable
fun ItemCard(item: Item) {
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
    Card(
        modifier = Modifier.padding(8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "List ID: ${item.listId}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Name: ${item.name}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

suspend fun fetchData(apiService: ApiService, items: MutableList<Item>, errorMessage: MutableState<String?>, isLoading: MutableState<Boolean>) {
    isLoading.value = true
    try {
        val fetchedItems = apiService.fetchItems()
            .filter { !it.name.isNullOrBlank() }
            .sortedWith(compareBy({ it.listId }, { it.name }))
        items.clear()
        items.addAll(fetchedItems)
        errorMessage.value = null
    } catch (e: Exception) {
        errorMessage.value = "Something went wrong."
    } finally {
        isLoading.value = false
    }
}
