import 'package:flutter/material.dart';

class BlockedScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[900],
      body: Center(
        child: Text("ผู้สูงอายุถูกบล็อก", style: TextStyle(fontSize: 24, color: Colors.white)),
      ),
    );
  }
}
