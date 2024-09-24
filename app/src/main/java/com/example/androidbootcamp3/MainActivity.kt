package com.example.androidbootcamp3

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        composable("game/{difficulty}") { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty")?.toInt() ?: 1
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
    var score by remember { mutableStateOf(0) }
    val targetScore = difficulty
    var options by remember { mutableStateOf(listOf("元気", "元氣", "π気", "π氣").shuffled()) }
    val viewModel: GameViewModel = viewModel() // ViewModel を取得

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.genkidama),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // 画面の端から少し余白を取る
                .aspectRatio(16 / 23f), // 画像のアスペクト比を設定
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

            // ボタン間のスペースを指定して選択肢を垂直に並べる
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp), // ボタン間のスペースを指定
            ) {
                options.forEach { option ->
                    Button(
                        onClick = {
                            if (option == "元気") {
                                score++
                                if (score >= targetScore) {
                                    // 結果画面にクリアしたかどうかのフラグを渡す際に、
                                    // パラメータをしっかりと渡すために "result?isWin=true" の形に変更
                                    navController.navigate("result?isWin=true")
                                }
                            } else {
                                // 間違えた場合に失敗画面に遷移
                                navController.navigate("result?isWin=false")
                            }
                            options = options.shuffled() //押した後再度シャッフル
                        },
                        modifier = Modifier
                            .fillMaxWidth() // ボタンを左右に広げる
                            .padding(horizontal = 16.dp), // 左右に余白を追加してバランスを取る
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF))
                    ) {
                        Text(option)
                    }
                }
            }
        }
    }
}

class GameViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isWin) {"クリア！"} else {"失敗…"},
                fontSize = 50.sp,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Cyan, // 縁取りの色
                        offset = Offset(2f, 5f), // 影をずらして縁取りの効果を出す
                        blurRadius = 1f // ぼかし具合を調整
                    )
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("difficulty") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
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

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        userList.forEach { user ->
            Text(text = "ユーザー名: ${user.username}, 難易度: ${user.difficulty}")
        }
    }
}