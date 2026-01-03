import 'package:flutter/material.dart';
import 'app.dart';
import 'services/call_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await CallService.initCallListener();
  runApp(MyApp());
}
