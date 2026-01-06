package com.riskguard.frontend.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.riskguard.frontend.R
import com.riskguard.frontend.model.FamilyMember
import com.riskguard.frontend.model.CallSession
import com.riskguard.frontend.network.MockApi

class OverlayChatActivity : AppCompatActivity() {

    private val familyMembers = listOf(
        FamilyMember("แม่", "123-456-7890"),
        FamilyMember("พ่อ", "987-654-3210")
    )

    private lateinit var tvStatus: TextView
    private lateinit var btnNext: Button
    private val answers = mutableMapOf<String,String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overlay_chat)

        tvStatus = findViewById(R.id.tvStatus)
        btnNext = findViewById(R.id.btnAction)

        val session = intent.getSerializableExtra("session") as? CallSession
        tvStatus.text = "โทรศัพท์จาก: ${session?.phoneNumber}"

        askCallerName()
    }

    private fun askCallerName() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("คนที่โทรมาคือใคร?")
            .setView(input)
            .setPositiveButton("ต่อไป") { _, _ ->
                answers["คนที่โทรมาคือใคร"] = input.text.toString()
                askRelationship()
            }
            .setCancelable(false)
            .show()
    }

    private fun askRelationship() {
        val options = arrayOf("รู้จัก","ไม่แน่ใจ","ไม่รู้จัก")
        AlertDialog.Builder(this)
            .setTitle("รู้จักเป็นการส่วนตัวไหม?")
            .setItems(options) { _, which ->
                answers["รู้จักเป็นการส่วนตัวไหม"] = options[which]
                askJob()
            }
            .setCancelable(false)
            .show()
    }

    private fun askJob() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("เขาบอกว่าทำงานอะไร?")
            .setView(input)
            .setPositiveButton("ต่อไป") { _, _ ->
                answers["เขาบอกว่าทำงานอะไร"] = input.text.toString()
                askRequest()
            }
            .setCancelable(false)
            .show()
    }

    private fun askRequest() {
        val options = arrayOf("โอนเงิน","ให้รหัส","ติดตั้งแอป","อื่นๆ")
        AlertDialog.Builder(this)
            .setTitle("เขาขอให้คุณทำอะไร?")
            .setItems(options) { _, which ->
                answers["เขาขอให้คุณทำอะไร"] = options[which]
                askUrgency()
            }
            .setCancelable(false)
            .show()
    }

    private fun askUrgency() {
        val options = arrayOf("มี","ไม่มี")
        AlertDialog.Builder(this)
            .setTitle("เขากดดันเรื่องความเร่งด่วนไหม?")
            .setItems(options) { _, which ->
                answers["เขากดดันเรื่องความเร่งด่วน"] = options[which]
                checkRisk()
            }
            .setCancelable(false)
            .show()
    }

    private fun checkRisk() {
        tvStatus.text = "กำลังวิเคราะห์ความเสี่ยง..."
        MockApi.evaluateRisk(answers) { result ->
            runOnUiThread {
                tvStatus.text = result.summary
                if(result.level == "HIGH") {
                    familyAlert()
                } else {
                    btnNext.text = "เสร็จสิ้น"
                    btnNext.setOnClickListener { finish() }
                }
            }
        }
    }

    private fun familyAlert() {
        val names = familyMembers.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("เลือกคนในครอบครัวเพื่อแจ้งเตือน")
            .setItems(names) { _, which ->
                val member = familyMembers[which]
                showFamilyDecision(member)
            }
            .setCancelable(false)
            .show()
    }

    private fun showFamilyDecision(member: FamilyMember) {
        AlertDialog.Builder(this)
            .setTitle("Family Alert สำหรับ ${member.name}")
            .setMessage("ใครโทรมา: ${answers["คนที่โทรมาคือใคร"]}\nความเสี่ยง: HIGH\nพฤติกรรม: ${answers["เขาขอให้คุณทำอะไร"]}")
            .setPositiveButton("✅ อนุญาตให้ทำต่อ") { _, _ ->
                finish() // จบ flow
            }
            .setNegativeButton("❌ ไม่อนุญาต / ติดต่อแม่ทันที") { _, _ ->
                // แจ้งเตือน
                AlertDialog.Builder(this)
                    .setMessage("${member.name}: ${answers["คนที่โทรมาคือใคร"]} เคสอันตราย!")
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .show()
            }
            .setCancelable(false)
            .show()
    }
}
