class MoneyService {
  static Future<bool> transferMoney(double amount, String toAccount) async {
    // call backend API
    await Future.delayed(Duration(seconds: 1));
    return true;
  }
}
