import 'package:flutter/material.dart';
import 'guardian_notify_screen.dart';

class RedAlertScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.red,
      body: Center(
        child: ElevatedButton(
          onPressed: () => Navigator.push(
              context, MaterialPageRoute(builder: (_) => GuardianNotifyScreen())),
          child: Text("แจ้ง Guardian"),
        ),
      ),
    );
  }
}
