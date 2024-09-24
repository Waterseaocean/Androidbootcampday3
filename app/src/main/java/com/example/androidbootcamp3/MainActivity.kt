package com.example.androidbootcamp3

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidbootcamp3.ui.theme.Androidbootcamp3Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Androidbootcamp3Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        GameApp()
                    }
                }
            }
        }
    }
}

@Composable
fun GameApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "start") {
        composable("start") { StartScreen(navController) }
        composable("difficulty") { DifficultySelectionScreen(navController) }
        composable(
            route = "game/{difficulty}",
            arguments = listOf(navArgument("difficulty") { type = NavType.IntType })
        ) { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getInt("difficulty") ?: 1
            GameScreen(navController, difficulty)
        }
        // NavHost の定義を変更して、isWin パラメータを適切に取得できるようにする
        composable(
            route = "result?isWin={isWin}",
            arguments = listOf(navArgument("isWin") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isWin = backStackEntry.arguments?.getBoolean("isWin") ?: true
            ResultScreen(navController, isWin)
        }
        composable("apiData") { ApiDataScreen(navController) }
    }
}

@Composable
fun StartScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.start_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(), // 画像を画面全体に広げる
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize() // Columnも画面全体に広げる
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // タイトル
            Text(
                text = "元気玉を作る\nゲーム",
                fontSize = 45.sp,
                color = Color.White,
                textAlign = TextAlign.Center, // 文字列を中央揃え
                lineHeight = 50.sp, // 行間を調整
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Cyan, // 縁取りの色
                        offset = Offset(2f, 2f), // 影をずらして縁取りの効果を出す
                        blurRadius = 10f // ぼかし具合を調整
                    )
                ),
                modifier = Modifier.padding(horizontal = 16.dp) // 左右に少し余白を追加してバランスを調整
            )

            // スペーサーで間隔を調整
            Spacer(modifier = Modifier.height(36.dp))

            // スタートボタン
            Button(onClick = { navController.navigate("difficulty") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)) {
                Text(text = "スタート", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // APIデータ画面に遷移するボタン
            Button(
                onClick = { navController.navigate("apiData") }, // apiData画面へ遷移
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text(text = "APIデータ", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DifficultySelectionScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(10) { index ->
            val difficulty = index + 1
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // ボタンの高さを指定
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("game/$difficulty")
                    }
                    .border(
                        width = 2.dp, // 枠線の太さ
                        color = Color.Blue, // 枠線の色
                        shape = RoundedCornerShape(8.dp) // 枠線の角を丸くする
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.bezita),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                Text(
                    text = "難易度 $difficulty",
                    fontSize = 24.sp,
                    color = Color.Cyan,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.White, // 縁取りの色
                            offset = Offset(2f, 2f), // 影をずらして縁取りの効果を出す
                            blurRadius = 1f // ぼかし具合を調整
                        )
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun GameScreen(navController: NavController, difficulty: Int) {
    val viewModel: GameViewModel = viewModel()
    val score by viewModel.score.collectAsState(initial = 0)
    val targetScore = difficulty
    var options by remember { mutableStateOf(listOf("元気", "元氣", "π気", "π氣").shuffled()) }

    // スコアのリセット
    LaunchedEffect(Unit) {
        viewModel.resetScore()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.genkidama),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // スコア表示
            Text(
                text = "集まった元気: $score",
                fontSize = 30.sp,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Cyan,
                        offset = Offset(2f, 5f),
                        blurRadius = 1f
                    )
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ボタンの選択肢を表示
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                options.forEach { option ->
                    Button(
                        onClick = {
                            if (option == "元気") {
                                val newScore = score + 1
                                viewModel.incrementScore()
                                if (newScore >= targetScore) {
                                    navController.navigate("result?isWin=true")
                                }
                            } else {
                                navController.navigate("result?isWin=false")
                            }
                            options = options.shuffled()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
                    ) {
                        Text(option)
                    }
                }
            }
        }
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.dataStore

    companion object {
        private val SCORE_KEY = intPreferencesKey("score")
    }

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    init {
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                preferences[SCORE_KEY] ?: 0
            }.collect { newScore ->
                _score.value = newScore
            }
        }
    }

    fun incrementScore() {
        _score.value++
        saveScore(_score.value)
    }

    fun resetScore() {
        _score.value = 0
        saveScore(0)
    }

    private fun saveScore(newScore: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[SCORE_KEY] = newScore
            }
        }
    }
}

@Composable
fun ResultScreen(navController: NavController, isWin: Boolean) {
    val viewModel: GameViewModel = viewModel()

    // スコアのリセット
    LaunchedEffect(Unit) {
        viewModel.resetScore()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isWin) "クリア！" else "失敗…",
                fontSize = 50.sp,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Cyan,
                        offset = Offset(2f, 5f),
                        blurRadius = 1f
                    )
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    navController.navigate("difficulty") // 難易度選択画面に戻る
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("もう一度プレイ")
            }
        }
    }
}

@Serializable
data class ApiDataresult(
    val username: String,
    val difficulty: Int,
)

@Composable
fun ApiDataScreen(navController: NavController) {
    val json = """
        [
            {"username":"USER1", "difficulty":3},
            {"username":"USER2", "difficulty":8}
        ]
    """
    val userList = Json.decodeFromString<List<ApiDataresult>>(json)

    // 背景色を追加するためにBoxを使用
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.start_image),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(), // 画像を画面全体に広げる
            contentScale = ContentScale.Crop
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp), // ユーザー間にスペースを追加
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(userList.size) { index ->
                val user = userList[index]

                // Cardを使って一つ一つのユーザーデータを整える
                Card(
                    modifier = Modifier
                        .fillMaxWidth() // カードを画面幅に広げる
                        .padding(8.dp) // 各カードにパディングを追加
                        .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp), // カードの角を丸くする
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp), // カードの内側にパディング
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ユーザー名の表示
                        Text(
                            text = "ユーザー名: ${user.username}",
                            fontSize = 20.sp,
                            color = Color.Black,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color.Gray,
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp)) // ユーザー名と難易度の間にスペース

                        // 難易度の表示
                        Text(
                            text = "難易度: ${user.difficulty}",
                            fontSize = 18.sp,
                            color = Color.Gray,
                        )
                    }
                }
            }
        }
    }
}