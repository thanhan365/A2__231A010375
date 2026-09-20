import 'package:flutter/material.dart';

void main() => runApp(const MyApp());

// 1. Chuyển MyApp thành StatefulWidget để lưu trạng thái _mode (Sáng/Tối)
class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  // Biến lưu trạng thái giao diện hiện tại (mặc định là light)
  ThemeMode _mode = ThemeMode.light;

  // Hàm callback để chuyển đổi qua lại giữa sáng và tối
  void _toggleTheme() {
    setState(() {
      _mode = _mode == ThemeMode.light ? ThemeMode.dark : ThemeMode.light;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Lab F1 – Hồ sơ',
      debugShowCheckedModeBanner: false,
      // Cấu hình Theme sáng
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.deepPurple,
          brightness: Brightness.light,
        ),
        useMaterial3: true,
      ),
      // Khai báo darkTheme theo yêu cầu
      darkTheme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.deepPurple,
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      // Gán themeMode dựa vào state _mode của ứng dụng
      themeMode: _mode,
      // Truyền hàm callback và trạng thái hiện tại xuống ProfilePage qua constructor
      home: ProfilePage(onThemeChanged: _toggleTheme, currentMode: _mode),
    );
  }
}

class ProfilePage extends StatefulWidget {
  // Nhận callback và currentMode từ MyApp qua constructor
  final VoidCallback onThemeChanged;
  final ThemeMode currentMode;

  const ProfilePage({
    super.key,
    required this.onThemeChanged,
    required this.currentMode,
  });

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  int _likes = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Lab F1 – Hồ sơ'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        actions: [
          // Đặt một IconButton trong actions của AppBar để gọi hàm đổi theme
          IconButton(
            icon: Icon(
              widget.currentMode == ThemeMode.light
                  ? Icons.dark_mode
                  : Icons.light_mode,
            ),
            onPressed: widget.onThemeChanged,
            tooltip: 'Chuyển đổi Sáng/Tối',
          ),
        ],
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            children: [
              const ProfileCard(
                name: 'Võ Thành Ân',
                studentId: '231A010375',
                className: 'Lớp CNTT – LTDD',
              ),
              const SizedBox(height: 20),
              Text(
                'Lượt thích: $_likes',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: _likes >= 10 ? Colors.red : null,
                ),
              ),
              const SizedBox(height: 15),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  ElevatedButton(
                    onPressed: () {
                      if (_likes > 0) setState(() => _likes--);
                    },
                    child: const Text('–'),
                  ),
                  const SizedBox(width: 10),
                  ElevatedButton.icon(
                    onPressed: () => setState(() => _likes++),
                    icon: const Icon(Icons.favorite, color: Colors.white),
                    label: const Text('Thích'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.indigo,
                      foregroundColor: Colors.white,
                    ),
                  ),
                  const SizedBox(width: 10),
                  ElevatedButton(
                    onPressed: () => setState(() => _likes = 0),
                    child: const Icon(Icons.refresh, size: 18),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class ProfileCard extends StatelessWidget {
  const ProfileCard({
    super.key,
    required this.name,
    required this.studentId,
    required this.className,
  });

  final String name;
  final String studentId;
  final String className;

  @override
  Widget build(BuildContext context) {
    final initial = name.trim().split(' ').last.substring(0, 1);

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            CircleAvatar(radius: 40, child: Text(initial)),
            const SizedBox(height: 10),
            Text(name, style: Theme.of(context).textTheme.titleLarge),
            Text('MSSV: $studentId'),
            Text(className),
          ],
        ),
      ),
    );
  }
}
