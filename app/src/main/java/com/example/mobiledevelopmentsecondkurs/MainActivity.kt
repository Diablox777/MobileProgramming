package com.example.mobiledevelopmentsecondkurs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.NewsRepositoryImpl
import com.example.mobiledevelopmentsecondkurs.ui.theme.MobileDevelopmentSecondKursTheme
import com.example.presentation.ui.MoonGLSurfaceView
import com.example.presentation.ui.MyGLSurfaceView
import com.example.presentation.ui.NeptuneGLSurfaceView
import com.example.presentation.ui.NewsScreen
import com.example.presentation.viewmodel.NewsViewModel
import com.example.presentation.viewmodel.NewsViewModelFactory

class MainActivity : ComponentActivity() {

    private val newsViewModel: NewsViewModel by viewModels {
        NewsViewModelFactory(NewsRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobileDevelopmentSecondKursTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen(newsViewModel = newsViewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(newsViewModel: NewsViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "menu"
    ) {
        composable("menu") { MenuScreen(navController) }
        composable("lab1") { NewsScreen(newsViewModel = newsViewModel) }
        composable("lab8") { Lab8Screen(navController) }
        /*composable("planetDetail/{planetName}/{planetInfo}/{isMoon}") { backStackEntry ->
            PlanetDetailScreen(
                planetName = backStackEntry.arguments?.getString("planetName") ?: "",
                planetInfo = backStackEntry.arguments?.getString("planetInfo") ?: "Информация отсутствует",
                isMoon = backStackEntry.arguments?.getString("isMoon") == "true"
            )
        }*/
    }
}

@Composable
fun MenuScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { navController.navigate("lab1") }) {
            Text(text = "Lab 1")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("lab8") }) { Text(text = "Lab 8") }
    }
}

@Composable
fun Lab8Screen(navController: NavController) {
    val context = LocalContext.current
    val glSurfaceView = remember { MyGLSurfaceView(context) }
    val planetsList = listOf("Меркурий", "Венера", "Земля", "Луна", "Марс", "Юпитер", "Сатурн", "Уран", "Нептун")
    val planetInfoMap = mapOf(
        "Меркурий" to Pair("Меркурий — самая маленькая планета в нашей Солнечной системе.", R.drawable.mercury_texture),
        "Венера" to Pair("Венера имеет густую, токсичную атмосферу.", R.drawable.venus_texture),
        "Земля" to Pair("Земля — третья планета от Солнца и наш дом.", R.drawable.earth_texture),
        "Луна" to Pair("Луна — единственный естественный спутник Земли.", R.drawable.moon_texture),
        "Марс" to Pair("Марс известен как Красная планета.", R.drawable.mars_texture),
        "Юпитер" to Pair("Юпитер — самая большая планета Солнечной системы.", R.drawable.jupiter_texture),
        "Сатурн" to Pair("Сатурн известен своей системой колец.", R.drawable.saturn_texture),
        "Уран" to Pair("Уран вращается на боку.", R.drawable.uranus_texture),
        "Нептун" to Pair("Нептун самая дальняя планета солнечной системы", R.drawable.neptune_texture)
    )

    var currentPlanetIndex by remember { mutableStateOf(2) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedPlanet by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        AndroidView(factory = { glSurfaceView }, modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { currentPlanetIndex = glSurfaceView.selectPreviousPlanet() }) { Text("Влево") }

            Button(onClick = {
                selectedPlanet = planetsList[currentPlanetIndex]
                showDialog = true
            }) { Text("Информация") }

            Button(onClick = { currentPlanetIndex = glSurfaceView.selectNextPlanet() }) { Text("Вправо") }
        }
    }

    if (showDialog) {
        if (selectedPlanet == "Луна") {
            MoonDetailScreen(onDismiss = { showDialog = false })
        }
        else if(selectedPlanet == "Нептун") {
            NeptuneDetailScreen(onDismiss = { showDialog = false })
        }
        else {
            val (planetInfo, planetImageRes) = planetInfoMap[selectedPlanet] ?: "Информация недоступна" to R.drawable.earth_texture
            PlanetInfoDialog(
                planetName = selectedPlanet,
                planetInfo = planetInfo,
                planetImageRes = planetImageRes,
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun PlanetInfoDialog(planetName: String, planetInfo: String, planetImageRes: Int, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Информация о $planetName", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = painterResource(id = planetImageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = planetInfo)
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) { Text("Закрыть") }
            }
        }
    }
}

@Composable
fun MoonDetailScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val moonGLSurfaceView = remember { MoonGLSurfaceView(context) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Изображение Луны с освещением по модели Фонга", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(16.dp))

                AndroidView(factory = { moonGLSurfaceView }, modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp))

                Spacer(modifier = Modifier.height(16.dp))

                // Описание Луны
                Text(
                    text = "Луна — единственный естественный спутник Земли. " +
                            "Она влияет на приливы и отливы и является объектом изучения многих космических миссий.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("Назад")
                }
            }
        }
    }
}

@Composable
fun NeptuneDetailScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val moonGLSurfaceView = remember { NeptuneGLSurfaceView(context) }
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                AndroidView(factory = { moonGLSurfaceView }, modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp))
                Spacer(modifier = Modifier.height(16.dp))
                // Описание Луны
                Text(
                    text = "Нептун самая дальняя планета солнечной системы",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Назад")
                }
            }
        }
    }
}


@Composable
fun PlanetDetailScreen(planetName: String, planetInfo: String, isMoon: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (isMoon) {
            AndroidView(factory = { MyGLSurfaceView(it) }, modifier = Modifier.fillMaxWidth().height(300.dp))
        } else {
            Text(text = "Изображение планеты $planetName")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = planetInfo, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().padding(16.dp))
    }
}
