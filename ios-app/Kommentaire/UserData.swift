//
//  UserData.swift
//  Kommentaire
//
//  Created by Martin on 06/07/2020.
//  Copyright © 2020 Kommentaire. All rights reserved.
//

import SwiftUI
import lib

final class UserData: ObservableObject  {
    @Published
    var loading: Bool = false
    @Published
    var error: Bool = false
    @Published
    var questions: [QuestionFragment] = []
    
    private let user = KomUser(pseudo: "iosUser", token: "`6h]fW>\\z@VWQQ6Z8n20200706134603")
    private let repository = KomRepository()
    
    func fetch() {
        ExtensionsKt.staticGetQuestions(repository: repository, user:  user) {
        //repository.getQuestions(user: user) {
            self.questions = $0
        }
    }

}

