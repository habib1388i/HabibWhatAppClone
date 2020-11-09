package com.example.habibwhatappclone.listener

interface ChatClickListener {
    fun onChatClicked(chatId: String?, otherUserId: String?, chatsImageUrl: String?, chatsName: String?)
}