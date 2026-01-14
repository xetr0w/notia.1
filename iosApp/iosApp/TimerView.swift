import SwiftUI

struct TimerView: View {
    var onNavigateBack: () -> Void
    
    var body: some View {
        VStack(spacing: 20) {
            Text("timer_xnote UI")
                .font(.largeTitle)
                .fontWeight(.bold)
            
            Text("Native SwiftUI View")
                .font(.subheadline)
                .foregroundColor(.gray)
            
            Text("25:00")
                .font(.system(size: 60, weight: .thin, design: .monospaced))
                .padding()
            
            Button(action: {
                onNavigateBack()
            }) {
                HStack {
                    Image(systemName: "arrow.left")
                    Text("Geri Dön")
                }
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(UIColor.systemBackground))
    }
}

#Preview {
    TimerView(onNavigateBack: {})
}
