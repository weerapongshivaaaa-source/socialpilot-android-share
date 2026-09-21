package ai.socialpilot.share
import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.content.Intent
import android.net.Uri
class MainActivity : Activity() {
 private lateinit var token:EditText
 private lateinit var status:TextView
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  val p=getSharedPreferences("socialpilot",MODE_PRIVATE)
  val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(32,32,32,32)}
  box.addView(TextView(this).apply{text="SocialPilot AI";textSize=24f})
  box.addView(TextView(this).apply{text="Connect this app once. Then Gallery → Share → SocialPilot AI posts directly."})
  status=TextView(this).apply{textSize=16f;setPadding(0,20,0,12)};box.addView(status)
  token=EditText(this).apply{hint="Mobile share token";setText(p.getString("share_token",""))};box.addView(token)
  box.addView(Button(this).apply{text="Save pairing token";setOnClickListener{saveToken()}})
  box.addView(Button(this).apply{text="Open SocialPilot pairing";setOnClickListener{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://socialpilot-ai-yvo2.hatchable.site/mobile-share.html?native=1")))}})
  box.addView(Button(this).apply{text="Clear pairing";setOnClickListener{p.edit().remove("share_token").apply();token.setText("");showStatus("Device unpaired")}})
  setContentView(box);handlePairIntent(intent);showStatus(if(token.text.toString().isBlank())"Not paired" else "✓ Device paired")
 }
 override fun onNewIntent(i:Intent?){super.onNewIntent(i);if(i!=null)handlePairIntent(i)}
 private fun handlePairIntent(i:Intent){if(i.action==Intent.ACTION_VIEW&&i.data?.scheme=="socialpilot"&&i.data?.host=="pair"){val t=i.data?.getQueryParameter("token")?.trim().orEmpty();if(t.isNotBlank()){token.setText(t);saveToken();showStatus("✓ Device paired from SocialPilot")}}}
 private fun saveToken(){val t=token.text.toString().trim();if(t.isBlank()){showStatus("Enter a pairing token first");return};getSharedPreferences("socialpilot",MODE_PRIVATE).edit().putString("share_token",t).apply();showStatus("✓ Device paired — Gallery sharing is ready")}
 private fun showStatus(s:String){status.text=s}
}
