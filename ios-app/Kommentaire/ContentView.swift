//
//  ContentView.swift
//  Kommentaire
//
//  Created by Martin on 06/07/2020.
//  Copyright © 2020 Kommentaire. All rights reserved.
//

import SwiftUI
import lib

struct ContentView: View {
    @EnvironmentObject var userData: UserData
    
    var body: some View {
        List(self.userData.questions, id: \.id) {questionFragment in
            Text("Hello, \(questionFragment.content)")
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        let userData = UserData()
        userData.questions = [
            QuestionFragment(__typename: "Question", id: 0, content: "test", votes: 2, userVoteType: .downvote, user: nil),
            QuestionFragment(__typename: "Question", id: 1, content: "Question 1", votes: 1, userVoteType: .downvote, user: nil),
        ]
        return ContentView()
            .environmentObject(userData)
    }
}
