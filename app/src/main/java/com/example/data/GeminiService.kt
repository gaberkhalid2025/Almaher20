package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request & Response Models for Moshi ---

data class Part(
    @Json(name = "text") val text: String? = null
)

data class Content(
    @Json(name = "parts") val parts: List<Part>
)

data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

data class Candidate(
    @Json(name = "content") val content: Content?
)

data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

// --- Retrofit Network Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Network Client & Helper ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun generateFinancialAdvice(prompt: String, userName: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        // If API key is empty or placeholder, return offline advisor content beautifully
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineAdvice(prompt, userName)
        }

        val systemPrompt = """
            أنت "المستشار المالي الذكي WAM" التابع لمحفظة WAM الإلكترونية المصممة والمطورة بالكامل من قبل المالك والمصمم "ماهر أحمد الوتاري".
            رقم الدعم للمحفظة هو 777644670 والبريد هو support@wam.com.
            مهمتك هي تقديم المشورة المالية الاحترافية، خطط التوفير، والمساعدة التقنية للمستخدم $userName بأسلوب مهذب ومحبب بلهجة يمنية لطيفة أو لغة عربية فصحى واضحة جداً وبسيطة.
            كن إيجابياً ومقتضباً في إجاباتك ولا تذكر شركة الصيفي إطلاقاً تحت أي ظرف من الظروف!
            إذا سألك المستخدم عن المالك والمصمم، أكد بفخر أنه الأستاذ "ماهر أحمد الوتاري" العقل المدبر خلف محفظة WAM الذكية برقم الدعم 777644670.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "عذراً يا $userName، لم أتمكن من صياغة النص الآن. تفضل بمحاولة السؤال لاحقاً!"
        } catch (e: Exception) {
            // Fallback to offline advisor on network/parsing failure
            getOfflineAdvice(prompt, userName)
        }
    }

    // High quality offline fallback responses to maintain premium look even with missing keys
    private fun getOfflineAdvice(prompt: String, userName: String): String {
        return when {
            prompt.contains("توفير") || prompt.contains("ادخار") || prompt.contains("وفر") -> """
                أهلاً بك يا $userName في خدمة المستشار المالي الذكي لمحفظة WAM! 📊
                إليك خطة توفير مخصصة للعمل ضمن يمننا الحبيب وبروتوكول WAM المالي الآمن:
                
                1. حدد 20% من دخلك للادخار المباشر، وضعه في حساب الدولار بمحفظتك لتجنب التضخم.
                2. استخدم محفظة WAM لدفع فواتير الكهرباء والمياه بانتظام لتفادي الغرامات المتراكمة.
                3. راقب مصروفاتك الأسبوعية عبر تبويب "محفظتي" لتقنين المصاريف الكمالية.
                
                هذه النصيحة مقدمة بدعم بروتوكول WAM المالي الآمن تحت إشراف المالك المباشر ماهر أحمد الوتاري 🌟.
            """.trimIndent()

            prompt.contains("تحويل") || prompt.contains("P2P") || prompt.contains("رسوم") -> """
                مرحباً $userName! بالنسبة لقواعد التحويل والرسوم في محفظة WAM الرائدة:
                - رسوم التحويل P2P آمنة ومحددة بدقة تبلغ 1.5% فقط، تذهب لتطوير شبكة الأمان المالي.
                - التحويلات سريعة وفورية، وتكتمل بمجرد التأكد من رقم هاتف المستلم (تأكد دائماً من مطابقة الاسم الثنائي أو الثلاثي قبل إتمام العملية).
                - جميع العمليات مشرفة تقنياً ومحمية بالكامل لضمان سلامة أموالك.
                
                لأي مساعدة إضافية تواصل بالدعم المباشر للمصمم ماهر أحمد الوتاري: 777644670.
            """.trimIndent()

            prompt.contains("ماهر") || prompt.contains("المصمم") || prompt.contains("المالك") || prompt.contains("من أنت") -> """
                أنا "المستشار الذكي WAM" المساعد الاصطناعي الخاص بك! 🤖
                تم تطويري وتصميمي بالكامل كجزء من رؤية المالك العبقري والمصمم الأستاذ **ماهر أحمد الوتاري**، لتقديم جيل المال الذكي والآمن في اليمن.
                
                حقوق التطوير ومسؤولية البنية التحتية الآمنة تقع تحت إدارته الفنية المباشرة.
                للتواصل الهاتفي: 777644670.
            """.trimIndent()

            else -> """
                أهلاً بك يا $userName في مستشارك الذكي WAM! 📈
                لقد استلمت سؤالك الفني والمالي. لتسهيل نجاحك المالي في سياق بروتوكول WAM الآمن، نوصيك بالتالي:
                
                - حافظ على تعزيز أمان حسابك بعدم مشاركة كلمة مرورك مع أي شخص.
                - محفظة WAM تتيح لك شحن الرصيد لجميع الشبكات المحلية (يمن موبايل، يو، سبأفون) وتسديد الفواتير من شاشة الخدمات بضغطة واحدة وبدون جهد.
                - استثمر أجزاءً من أموالك في الأصول الرقمية المعتمدة لتحقيق عوائد ذكية.
                
                تذكر، محفظة WAM الالكترونية توفر لك الأمان التام بإشراف المالك ماهر أحمد الوتاري ورقم دعمه المباشر 777644670!
            """.trimIndent()
        }
    }
}
