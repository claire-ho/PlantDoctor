package com.example.plantdoctor2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.plantdoctor2.ui.theme.PlantDoctorTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass

class MainPageActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            PlantDoctorApp(windowSizeClass)
        }
    }
}

@Composable
fun PlantDoctorApp(windowSize: WindowSizeClass) {
    when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            PlantDoctorAppPortrait()
        }
        WindowWidthSizeClass.Expanded -> {
            PlantDoctorAppLandscape()
        }
    }
}



// Step: Search bar - Modifiers
@Composable
fun SearchBar(
    modifier: Modifier = Modifier
) {
    TextField(
        value = "",
        onValueChange = {},
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        placeholder = {
            Text(stringResource(R.string.search_hint))
        },
        modifier =  modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    )
}

private data class DrawableStringPair(
    @DrawableRes val drawable: Int,
    @StringRes val text: Int
)

private val detectionCategories = listOf(
    R.drawable.leaves to R.string.category_leaves,
    R.drawable.stem to R.string.category_stems,
    R.drawable.flowers to R.string.category_flowers,
    R.drawable.peach to R.string.category_fruits,
    R.drawable.pest to R.string.category_pests,
).map { DrawableStringPair(it.first, it.second) }


private val plantList = listOf(
    R.drawable.passionfruit1 to R.string.plant_passion_fruit,
    R.drawable.strawberries to R.string.plant_strawberry,
    R.drawable.mangotree to R.string.plant_mango,
    R.drawable.pineappletree to R.string.plant_pineapple,
    R.drawable.appletree to R.string.plant_apple,
    R.drawable.bannatree to R.string.plant_banana,
).map { DrawableStringPair(it.first, it.second) }

@Composable
fun DetectionCategoryElement(
    @DrawableRes drawable: Int,
    @StringRes text: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Log.d("MainPageActivity", "Category element:" + stringResource(text))
        Image(
            painter = painterResource(drawable),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
        )
        Text(
            text = stringResource(text),
            modifier = Modifier.paddingFromBaseline(top = 24.dp, bottom = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DetectionCategoriesRow(
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = modifier
    ) {
        items(detectionCategories) { item ->
            DetectionCategoryElement(item.drawable, item.text, Modifier.height(120.dp))
        }
    }
}

@Composable
fun PlantCard(
    @DrawableRes drawable: Int,
    @StringRes text: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(255.dp)
        ) {
            Image(
                painter = painterResource(drawable),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = stringResource(text),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun PlantsGrid(
    modifier: Modifier = Modifier
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.height(168.dp)
    ) {
        items(plantList) { item ->
            PlantCard(item.drawable, item.text, Modifier.height(80.dp))
        }
    }
}

@Preview(apiLevel = 34, showBackground = true, backgroundColor = 0xFFF5F0EE)
@Composable
fun DetectionCategoryElementPreview() {
    PlantDoctorTheme {
        DetectionCategoryElement(
            text = R.string.category_leaves,
            drawable = R.drawable.leaves,
            modifier = Modifier.padding(8.dp)
        )
    }
}


// Step: Bottom navigation - Material
@Composable
private fun SootheBottomNavigation(modifier: Modifier = Modifier) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.app_name)
                )
            },
            selected = true,
            onClick = {}
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.take_photo)
                )
            },
            selected = false,
            onClick = {}
        )
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Coffee,
                    contentDescription = null
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.chat_with_robot)
                )
            },
            selected = false,
            onClick = {}
        )
    }
}

@Composable
private fun SootheNavigationRail(modifier: Modifier = Modifier) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
        ) {
            NavigationRailItem(
                modifier = Modifier.paddingFromBaseline(80.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null
                    )
                },
                label = {
                    Text(stringResource(R.string.app_name))
                },
                selected = true,
                onClick = {}
            )

            NavigationRailItem(
                modifier = Modifier.paddingFromBaseline(80.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null
                    )
                },
                label = {
                    Text(stringResource(R.string.take_photo))
                },
                selected = false,
                onClick = {}
            )
            NavigationRailItem(
                modifier = Modifier.paddingFromBaseline(80.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = stringResource(R.string.chat_with_robot)
                    )
                },
                selected = false,
                onClick = {}
            )
        }
    }
}

@Composable
fun HomeSection(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .paddingFromBaseline(top = 40.dp, bottom = 16.dp)
                .padding(horizontal = 16.dp)
        )
        content()
    }
}

@Preview(apiLevel = 34, showBackground = true, backgroundColor = 0xFFF5F0EE)
@Composable
fun DetectionCategoriesRowPreview() {
    PlantDoctorTheme {
        DetectionCategoriesRow()
    }
}

@Preview(apiLevel = 34, showBackground = true, backgroundColor = 0xFFF5F0EE)
@Composable
fun PlantCardPreview() {
    PlantDoctorTheme {
        PlantCard(
            text = R.string.plant_passion_fruit,
            drawable = R.drawable.passionfruit1,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0EE)
@Composable
fun PlantsGridPreview() {
    PlantDoctorTheme { PlantsGrid() }
}

@Preview(apiLevel = 34, showBackground = true, backgroundColor = 0xFFF5F0EE)
@Composable
fun HomeSectionPreview() {
    PlantDoctorTheme {
        HomeSection(R.string.select_category) {
            DetectionCategoriesRow()
        }
    }
}

// Step: Home screen - Scrolling
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(modifier.verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        SearchBar(Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(16.dp))
        HomeSection(title = R.string.select_category) {
            DetectionCategoriesRow()
        }
        Spacer(Modifier.height(24.dp))
        HomeSection(title = R.string.select_plant) {
            PlantsGrid()
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Preview(apiLevel = 34, showBackground = true, backgroundColor = 0xFFF5F0EE)
@Composable
fun SearchBarPreview() {
    PlantDoctorTheme { SearchBar(Modifier.padding(8.dp)) }
}


@Composable
fun PlantDoctorAppPortrait() {
    PlantDoctorTheme {
        Scaffold(
            bottomBar = { SootheBottomNavigation() }
        ) { padding ->
            HomeScreen(Modifier.padding(padding))
        }
    }
}

@Composable
fun PlantDoctorAppLandscape(){
    PlantDoctorTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row {
                SootheNavigationRail()
                HomeScreen()
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun PlantDoctorAppPortraitPreview() {
    PlantDoctorAppPortrait()
}

@Preview(widthDp = 640, heightDp = 360)
@Composable
fun PlantDoctorAppLandscapePreview() {
    PlantDoctorAppLandscape()
}
