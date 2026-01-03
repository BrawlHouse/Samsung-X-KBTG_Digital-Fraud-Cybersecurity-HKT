import 'package:flutter/material.dart';
import 'blocked_screen.dart';

class GuardianNotifyScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text("Guardian Notification")),
      body: Center(
        child: ElevatedButton(
          onPressed: () => Navigator.push(
              context, MaterialPageRoute(builder: (_) => BlockedScreen())),
          child: Text("Reject & Block ผู้สูงอายุ"),
        ),
      ),
    );
  }
}
