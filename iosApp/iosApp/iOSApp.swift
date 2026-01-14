import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    
    init() {
        IosTimerProvider.shared.timerScreenFactory = { onNavigateBack in
            return UIHostingController(rootView: TimerView(onNavigateBack: { _ = onNavigateBack() }))
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
