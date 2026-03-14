import { useState } from "react";
import api from "../api/axios";

const ChatPage = () => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [conversationId, setConversationId] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSend = async () => {
    const newMessage = { role: "user", content: input };
    setLoading(true);
    setMessages([...messages, newMessage]);
    setInput("");
    try {
      let response;
      if (conversationId == null) {
        response = await api.post("/api/messages", {
          content: input,
          modelName: "gemini-3-flash-preview",
        });
        setConversationId(response.data.conversationId);
        console.log(response.data);
      } else {
        response = await api.post("/api/messages/" + conversationId, {
          content: input,
          modelName: "gemini-3-flash-preview",
        });
        console.log(response.data);
      }
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: response.data.content },
      ]);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <div>
        {messages.map((msg, index) => (
          <div key={index}>
            <strong>{msg.role}: </strong>
            <strong>{msg.content}</strong>
          </div>
        ))}
      </div>
      <input
        type="text"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="Type a message..."
      />
      <button onClick={handleSend} disabled={loading}>
        Send
      </button>
    </div>
  );
};

export default ChatPage;
