import 'package:flutter/material.dart';
import 'transfer_money_screen.dart';
import 'red_alert_screen.dart';

class IncomingCallScreen extends StatelessWidget {
  void _receiveCall(BuildContext context) {
    Navigator.push(context, MaterialPageRoute(builder: (_) => TransferMoneyScreen()));
  }

  void _rejectCall(BuildContext context) {
    Navigator.push(context, MaterialPageRoute(builder: (_) => RedAlertScreen()));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Incoming Call")),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            ElevatedButton(onPressed: () => _receiveCall(context), child: Text("รับสาย")),
            ElevatedButton(onPressed: () => _rejectCall(context), child: Text("ปฏิเสธ")),
          ],
        ),
      ),
    );
  }
}
