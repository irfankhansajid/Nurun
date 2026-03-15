import { useState, useEffect, useRef } from "react";
import api from "../api/axios";
import { useNavigate } from "react-router-dom";
import ReactMarkdown from 'react-markdown'

const ChatPage = () => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState("");
  const [conversationId, setConversationId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");


  const [conversations, setConversations] = useState([]);

  const bottomRef = useRef(null);
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    api.get("/api/conversations")
    .then((res) => setConversations(res.data))
   .catch((error) => setError("Failed to load conversations"));
  }, []);

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
        const newTitle = input.length > 50 ? input.substring(0, 47) + "..." : input;
        setConversations((prev) => [{ id: response.data.conversationId, title: newTitle }, ...prev]);
      } else {
        response = await api.post("/api/messages/" + conversationId, {
          content: input,
          modelName: "gemini-3-flash-preview",
        });
      }
      setMessages((prev) => [
        ...prev,
        { role: "assistant", content: response.data.content, modelUsed: response.data.modelUsed, providerUsed: response.data.providerUsed },
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

  const handleSelectConversation = async (convId) => {
    setConversationId(convId);
    setMessages([]);
    setError("");
    setLoading(true);
    try {
      const response = await api.get(`/api/conversations/${convId}/messages`);
      const mapped = response.data.map((msg) => ({
        role: msg.messageRole.toLowerCase(),
        content: msg.content,
        modelUsed: msg.modelUsed,
        providerUsed: msg.providerUsed,
      }));
      setMessages(mapped);
    } catch (error) {
      setError("Failed to load conversation messages");
    } finally {
      setLoading(false);
    }
  };


  return (
    <div className="flex h-screen bg-gray-100">

      {/* Sidebar */}
      <div className={`${sidebarOpen ? 'flex' : 'hidden'} md:flex w-64 flex-col bg-gray-900 text-white`}>
        <div className="p-3 border-b border-gray-700">
          <span className="text-sm font-semibold text-gray-300 px-1">Nurun</span>
          <button
            onClick={handleNewChat}
            className="mt-2 w-full flex items-center gap-2 px-3 py-2 text-sm rounded-md border border-gray-600 hover:bg-gray-700 text-gray-200"
          >
            + New chat
          </button>
        </div>

        <div className="flex-1 overflow-y-auto py-2">
          <p className="px-3 py-1 text-xs text-gray-500 uppercase tracking-wider">Recent</p>
          {conversations.length === 0 && (
            <p className="px-3 py-2 text-xs text-gray-500">No conversations yet</p>
          )}
          {conversations.map((conv) => (
            <button
              key={conv.id}
              onClick={() => handleSelectConversation(conv.id)}
              className={`w-full text-left px-3 py-2 text-sm truncate rounded-md mx-1 hover:bg-gray-700 text-gray-300 ${
                conv.id === conversationId ? "bg-gray-700 text-white" : ""
              }`}
            >
              {conv.title}
            </button>
          ))}
        </div>

        <div className="p-3 border-t border-gray-700">
          <button
            onClick={handleLogout}
            className="w-full text-left px-3 py-2 text-sm text-gray-400 hover:bg-gray-700 rounded-md"
          >
            Logout
          </button>
        </div>
      </div>

      {/* Main */}
      <div className="flex-1 flex flex-col">
        <div className="md:hidden flex items-center px-4 py-3 bg-white border-b border-gray-200">
          <button onClick={() => setSidebarOpen(!sidebarOpen)} className="text-gray-700 text-xl">
            ☰
          </button>
        </div>
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
              <ReactMarkdown>{msg.content}</ReactMarkdown>
              {msg.modelUsed && (
                <div className="text-xs text-gray-500 mt-2">
                  Model: {msg.modelUsed}
                </div>
              )}
              {msg.providerUsed && (
                <div className="text-xs text-gray-500">
                  Provider: {msg.providerUsed}
                </div>
              )}
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
    </div>
  );
};

export default ChatPage;
