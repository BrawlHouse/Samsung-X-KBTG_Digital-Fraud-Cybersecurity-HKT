import 'package:flutter/material.dart';
import 'screens/incoming_call_screen.dart';

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Demo Killer',
      theme: ThemeData(primarySwatch: Colors.red),
      home: IncomingCallScreen(),
      debugShowCheckedModeBanner: false,
    );
  }
}
