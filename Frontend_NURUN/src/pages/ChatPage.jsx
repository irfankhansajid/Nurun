import { useState, useEffect, useRef } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";

const ChatPage = () => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [conversationId, setConversationId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const bottomRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || loading) return;
    const newMessage = { role: "user", content: input };
    setLoading(true);
    setError("");
    setMessages((prev) => [...prev, newMessage]);
    setInput("");
    try {
      let response;
      if (conversationId == null) {
        response = await api.post("/api/messages", {
          content: input,
          modelName: "gemini-3-flash-preview",
        });
        setConversationId(response.data.conversationId);
      } else {
        response = await api.post("/api/messages/" + conversationId, {
          content: input,
          modelName: "gemini-3-flash-preview",
        });
      }
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: response.data.content },
      ]);
    } catch (err) {
      setError("Something went wrong. Try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleNewChat = () => {
    setMessages([]);
    setConversationId(null);
    setError("");
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <div className="flex flex-col h-screen bg-gray-100">
      {/* top */}
      <div className="flex items-center justify-between px-6 py-3 bg-white border-b border-gray-200">
        <span className="text-lg font-bold">Nurun</span>
        <div className="flex gap-3">
          <button
            onClick={handleNewChat}
            className="px-4 py-1.5 text-sm border border-gray-300 rounded hover:bg-gray-50"
          >
            New Chat
          </button>
          <button
            onClick={handleLogout}
            className="px-4 py-1.5 text-sm border border-gray-300 rounded hover:bg-gray-50"
          >
            Logout
          </button>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto px-4 py-6 flex flex-col gap-3">
        {messages.length === 0 && !loading && (
          <p className="text-center text-gray-400 mt-20">
            Ask anything. Your conversation is saved.
          </p>
        )}

        {messages.map((msg, index) => (
          <div
            key={index}
            className={`max-w-2xl px-4 py-3 rounded-lg text-sm leading-relaxed whitespace-pre-wrap ${
              msg.role === "user"
                ? "bg-gray-900 text-white self-end ml-auto"
                : "bg-white text-gray-900 self-start border border-gray-200"
            }`}
          >
            <span className="block text-xs font-bold uppercase opacity-50 mb-1">
              {msg.role === "user" ? "You" : "Nurun"}
            </span>
            {msg.content}
          </div>
        ))}

        {loading && (
          <div className="max-w-2xl px-4 py-3 rounded-lg text-sm bg-white border border-gray-200 self-start">
            <span className="block text-xs font-bold uppercase opacity-50 mb-1">
              Nurun
            </span>
            <span className="text-gray-400">Thinking...</span>
          </div>
        )}

        {error && <p className="text-center text-red-500 text-sm">{error}</p>}

        <div ref={bottomRef} />
      </div>

      {/* input */}

      <div className="flex gap-3 px-4 py-3 bg-white border-t border-gray-200">
        <textarea
          className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm resize-none outline-none font-sans leading-relaxed"
          rows={2}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Type your message... (Enter to send)"
          disabled={loading}
        />
        <button
          onClick={handleSend}
          disabled={loading || !input.trim()}
          className="px-5 py-2 bg-gray-900 text-white text-sm font-semibold rounded-lg self-end disabled:opacity-40"
        >
          Send
        </button>
      </div>
    </div>
  );
};

export default ChatPage;
