import 'package:flutter/material.dart';
import 'red_alert_screen.dart';
import '../services/money_service.dart';

class TransferMoneyScreen extends StatelessWidget {
  void _confirmTransfer(BuildContext context) async {
    await MoneyService.transferMoney(1000, "12345678");
    Navigator.push(context, MaterialPageRoute(builder: (_) => RedAlertScreen()));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("โอนเงิน")),
      body: Center(
        child: ElevatedButton(onPressed: () => _confirmTransfer(context), child: Text("ยืนยันโอน")),
      ),
    );
  }
}
