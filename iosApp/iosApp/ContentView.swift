import UIKit
import SwiftUI
import ComposeApp

// Matches MythColors.BgAbyss (#030616) — used behind the Compose host so no
// white flashes through during launch / first frame.
private let appBackground = Color(
    red: 0x03 / 255.0,
    green: 0x06 / 255.0,
    blue: 0x16 / 255.0
)

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ZStack {
            appBackground.ignoresSafeArea()
            ComposeView()
                .ignoresSafeArea()
        }
        .background(appBackground.ignoresSafeArea())
    }
}
