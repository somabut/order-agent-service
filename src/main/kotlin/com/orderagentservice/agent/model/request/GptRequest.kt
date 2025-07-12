package com.orderagentservice.agent.model.request

data class GptRequest(
    val model: String,
    val messages: List<Message>
)

data class Message(
    val role: String,
    val content: String
)


//curl https://api.openai.com/v1/chat/completions \
//-H "Content-Type: application/json" \
//-H "Authorization: Bearer $OPENAI_API_KEY" \
//-d '{
//"model": "gpt-3.5-turbo",
//"messages": [
//{
//    "role": "user",
//    "content": "현재 한국의 온도는 어떤가요?"
//}
//],
//"temperature": 1,
//"max_tokens": 256,
//"top_p": 1,
//"frequency_penalty": 0,
//"presence_penalty": 0
//}'