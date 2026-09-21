package ai.socialpilot.share

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.widget.Toast
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class ShareReceiverActivity: Activity() {
 private val endpoint="https://socialpilot-ai-yvo2.hatchable.site/api/share/ingest"
 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState)
  val prefs=getSharedPreferences("socialpilot",MODE_PRIVATE)
  val token=prefs.getString("share_token","") ?: ""
  val cookie=CookieManager.getInstance().getCookie("https://socialpilot-ai-yvo2.hatchable.site/") ?: ""
  val uris=when(intent.action){
   Intent.ACTION_SEND_MULTIPLE -> intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) ?: arrayListOf()
   Intent.ACTION_SEND -> listOfNotNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
   else -> emptyList()
  }
  if(uris.isEmpty()){Toast.makeText(this,"No photo or video was received.",Toast.LENGTH_LONG).show();finish();return}
  Thread{
   try{
    val result=uploadWithAuth(uris.take(10),token,cookie)
    runOnUiThread{Toast.makeText(this,"SocialPilot: $result",Toast.LENGTH_LONG).show();finish()}
   }catch(e:Exception){
    runOnUiThread{Toast.makeText(this,"SocialPilot: "+e.message,Toast.LENGTH_LONG).show();finish()}
   }
  }.start()
 }
 private fun uploadWithAuth(uris:List<Uri>,token:String,cookie:String):String{
  var lastError:Exception?=null
  if(token.isNotBlank()){
   try{return uploadOnce(uris,"Bearer $token",null)}
   catch(e:Exception){
    lastError=e
    if(!e.message.orEmpty().contains("HTTP 401")) throw e
    getSharedPreferences("socialpilot",MODE_PRIVATE).edit().remove("share_token").apply()
   }
  }
  if(cookie.isNotBlank()){
   try{return uploadOnce(uris,null,cookie)}
   catch(e:Exception){lastError=e}
  }
  throw lastError ?: IllegalStateException("Open SocialPilot in this app and sign in before sharing from Gallery.")
 }
 private fun uploadOnce(uris:List<Uri>,authorization:String?,cookie:String?):String{
  val boundary="SocialPilot-"+UUID.randomUUID()
  val c=URL(endpoint).openConnection() as HttpURLConnection
  c.requestMethod="POST";c.doOutput=true;c.connectTimeout=30000;c.readTimeout=120000
  if(!authorization.isNullOrBlank()) c.setRequestProperty("Authorization",authorization)
  if(cookie.isNotBlank()) c.setRequestProperty("Cookie",cookie)
  c.setRequestProperty("Content-Type","multipart/form-data; boundary=$boundary")
  DataOutputStream(c.outputStream).use{out->
   var sent=0
   for((i,uri) in uris.withIndex()){
    val mime=contentResolver.getType(uri) ?: continue
    if(!mime.startsWith("image/")&&!mime.startsWith("video/")) continue
    out.writeBytes("--$boundary\r\n")
    out.writeBytes("Content-Disposition: form-data; name=\"media\"; filename=\"shared-" + (i+1) + "\"\r\n")
    out.writeBytes("Content-Type: $mime\r\n\r\n")
    contentResolver.openInputStream(uri)?.use{input->input.copyTo(out);sent++}
    out.writeBytes("\r\n")
   }
   out.writeBytes("--$boundary--\r\n");out.flush()
   if(sent==0) throw IllegalStateException("No supported image/video files")
  }
  val code=c.responseCode
  val stream=if(code in 200..299)c.inputStream else c.errorStream
  val body=stream?.bufferedReader()?.use{it.readText()} ?: ""
  c.disconnect()
  if(code==413) throw IllegalStateException("This media file is too large. Use a social-ready image/video under 25 MB.")
  if(code==401) throw IllegalStateException("HTTP 401")
  if(code !in 200..299) throw IllegalStateException("HTTP $code "+body.take(220))
  return "posted successfully"
 }
}
