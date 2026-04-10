import { memo, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import ReactMarkdown from "react-markdown";
import api from "../api/axios";

const MODELS = ["nurun-auto", "Groq", "Gemini"];
const isDark = (theme) => theme === "dark";
const INITIAL_VISIBLE_MESSAGES = 50;
const LOAD_MORE_BATCH = 25;
const CONVERSATION_ACTIVITY_KEY = "conversationLastUsedAt";

const loadConversationActivity = () => {
  try {
    const raw = localStorage.getItem(CONVERSATION_ACTIVITY_KEY);
    if (!raw) return {};
    const parsed = JSON.parse(raw);
    return parsed && typeof parsed === "object" ? parsed : {};
  } catch {
    return {};
  }
};

const sortConversationsByRecent = (items) =>
  [...items].sort((a, b) => {
    const aTime = new Date(a.lastUsedAt || a.createdAt || 0).getTime();
    const bTime = new Date(b.lastUsedAt || b.createdAt || 0).getTime();
    return bTime - aTime;
  });

const formatTime = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "";
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
};

const getInitials = (text) => {
  if (!text) return "N";
  return text.trim().slice(0, 1).toUpperCase();
};

const markdownToPlainText = (markdown) => {
  if (!markdown) return "";

  return markdown
    .replace(/```[\s\S]*?```/g, (block) => block.replace(/```/g, ""))
    .replace(/`([^`]+)`/g, "$1")
    .replace(/!\[[^\]]*\]\([^)]*\)/g, "")
    .replace(/\[([^\]]+)\]\([^)]*\)/g, "$1")
    .replace(/^\s{0,3}#{1,6}\s+/gm, "")
    .replace(/^\s{0,3}>\s?/gm, "")
    .replace(/^\s*[-*+]\s+/gm, "- ")
    .replace(/^\s*\d+\.\s+/gm, "")
    .replace(/\*\*([^*]+)\*\*/g, "$1")
    .replace(/\*([^*]+)\*/g, "$1")
    .replace(/_([^_]+)_/g, "$1")
    .replace(/~~([^~]+)~~/g, "$1")
    .replace(/\n{3,}/g, "\n\n")
    .trim();
};

const MarkdownWithCopy = memo(function MarkdownWithCopy({ content, dark }) {
  const markdownComponents = useMemo(
    () => ({
      code({ inline, className, children }) {
        const text = String(children).replace(/\n$/, "");

        if (inline) {
          return (
            <code
              className={`rounded px-1 py-0.5 ${
                dark ? "bg-[#2f2f2f] text-[#ececec]" : "bg-[#e5e7eb] text-[#111827]"
              }`}
            >
              {children}
            </code>
          );
        }

        return (
          <div
            className={`mt-2 overflow-hidden rounded-lg border ${
              dark ? "border-[#303030] bg-[#171717]" : "border-[#d1d5db] bg-[#f9fafb]"
            }`}
          >
            <div
              className={`flex items-center justify-between px-3 py-2 ${
                dark ? "border-b border-[#303030]" : "border-b border-[#d1d5db]"
              }`}
            >
              <span className={`text-xs ${dark ? "text-[#b4b4b4]" : "text-[#6b7280]"}`}>code</span>
              <button
                onClick={() => navigator.clipboard.writeText(text)}
                className={`text-xs ${
                  dark
                    ? "text-[#b4b4b4] hover:text-[#ececec]"
                    : "text-[#6b7280] hover:text-[#111827]"
                }`}
              >
                Copy
              </button>
            </div>
            <pre
              className={`overflow-x-auto p-3 text-xs leading-relaxed ${
                dark ? "text-[#ececec]" : "text-[#111827]"
              }`}
            >
              <code className={className}>{children}</code>
            </pre>
          </div>
        );
      },
    }),
    [dark]
  );

  return <ReactMarkdown components={markdownComponents}>{content}</ReactMarkdown>;
});

const MessageItem = memo(function MessageItem({ message, dark, messageKey, copiedMessageKey, onCopyMessage }) {
  const isUser = message.role === "user";

  return (
    <div className="w-full">
      <div className={`flex ${isUser ? "justify-end" : "justify-start"}`}>
        <div
          className={`max-w-[92%] px-4 py-3 text-sm leading-relaxed ${
            isUser
              ? dark
                ? "rounded-2xl bg-[#2f2f2f] text-[#ececec]"
                : "rounded-2xl bg-[#f3f4f6] text-[#111827]"
              : dark
                ? "rounded-xl text-[#ececec]"
                : "rounded-xl text-[#111827]"
          }`}
        >
          {!isUser && (
            <div
              className={`mb-2 flex items-center gap-2 text-xs ${
                dark ? "text-[#b4b4b4]" : "text-[#6b7280]"
              }`}
            >
              <span
                className={`inline-flex h-5 w-5 items-center justify-center rounded-full text-[10px] ${
                  dark ? "bg-[#2f2f2f]" : "bg-[#e5e7eb]"
                }`}
              >
                N
              </span>
              <span>Nurun</span>
            </div>
          )}

          {isUser ? (
            <p className="whitespace-pre-wrap">{message.content}</p>
          ) : (
            <div
              className={`space-y-2 leading-7 [&_h1]:mb-2 [&_h1]:mt-4 [&_h1]:text-xl [&_h1]:font-semibold [&_h2]:mb-2 [&_h2]:mt-4 [&_h2]:text-lg [&_h2]:font-semibold [&_h3]:mb-2 [&_h3]:mt-3 [&_h3]:font-semibold [&_li]:ml-5 [&_li]:list-disc [&_p]:mb-3 [&_pre]:my-3`}
            >
              <MarkdownWithCopy content={message.content} dark={dark} />
            </div>
          )}

          <div className={`mt-2 text-[11px] ${dark ? "text-[#b4b4b4]" : "text-[#6b7280]"}`}>
            {formatTime(message.sentAt)}
          </div>

          {!isUser && (
            <div
              className={`mt-3 flex items-center gap-2 pt-1 text-[11px] ${
                dark ? "text-[#b4b4b4]" : "text-[#6b7280]"
              }`}
            >
              {message.providerUsed && (
                <span className="rounded-full py-0.5 uppercase tracking-wide">
                  {message.providerUsed}
                </span>
              )}

              {message.modelUsed && (
                <span className="rounded-full py-0.5 uppercase tracking-wide">
                  {message.modelUsed}
                </span>
              )}

              <button
                type="button"
                onClick={() => onCopyMessage(markdownToPlainText(message.content), messageKey)}
                className={`rounded-md border px-2 py-0.5 ${
                  dark
                    ? "border-[#303030] hover:bg-[#202123] hover:text-[#ececec]"
                    : "border-[#d1d5db] hover:bg-[#f3f4f6] hover:text-[#111827]"
                }`}
              >
                {copiedMessageKey === messageKey ? "Copied" : "Copy text"}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
});

const ChatPage = ({ theme, toggleTheme }) => {
  const [messages, setMessages] = useState([]);
  const [conversations, setConversations] = useState([]);
  const [input, setInput] = useState("");
  const [currentConversationId, setCurrentConversationId] = useState(null);
  const [selectedModel, setSelectedModel] = useState("nurun-auto");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [menuOpenId, setMenuOpenId] = useState(null);
  const [renameId, setRenameId] = useState(null);
  const [renameText, setRenameText] = useState("");
  const [visibleCount, setVisibleCount] = useState(INITIAL_VISIBLE_MESSAGES);
  const [autoScrollEnabled, setAutoScrollEnabled] = useState(true);
  const [showScrollToBottom, setShowScrollToBottom] = useState(false);
  const [conversationActivity, setConversationActivity] = useState(loadConversationActivity);
  const [copiedMessageKey, setCopiedMessageKey] = useState("");

  const textareaRef = useRef(null);
  const bottomRef = useRef(null);
  const messageContainerRef = useRef(null);
  const userInteractedScrollRef = useRef(false);
  const loadingOlderRef = useRef(false);
  const restoreScrollRef = useRef(null);
  const activeRequestControllerRef = useRef(null);
  const navigate = useNavigate();

  const userEmail = useMemo(() => localStorage.getItem("email") || "user@nurun.ai", []);
  const userName = useMemo(() => userEmail.split("@")[0] || "Nurun User", [userEmail]);
  const dark = isDark(theme);

  const saveConversationActivity = useCallback((updater) => {
    setConversationActivity((prev) => {
      const next = updater(prev);
      localStorage.setItem(CONVERSATION_ACTIVITY_KEY, JSON.stringify(next));
      return next;
    });
  }, []);

  const markConversationAsUsed = useCallback(
    (conversationId, at = new Date().toISOString()) => {
      if (conversationId == null) return;

      saveConversationActivity((prev) => ({
        ...prev,
        [conversationId]: at,
      }));

      setConversations((prev) =>
        sortConversationsByRecent(
          prev.map((item) =>
            item.id === conversationId
              ? {
                  ...item,
                  lastUsedAt: at,
                }
              : item
          )
        )
      );
    },
    [saveConversationActivity]
  );

  useEffect(() => {
    const fetchConversations = async () => {
      try {
        const response = await api.get("/api/conversations");
        const merged = (response.data || []).map((item) => ({
          ...item,
          lastUsedAt: conversationActivity[item.id] || item.createdAt,
        }));
        setConversations(sortConversationsByRecent(merged));
      } catch {
        setError("Failed to load conversations.");
      }
    };

    fetchConversations();
  }, [conversationActivity]);

  useEffect(() => {
    if (!autoScrollEnabled) return;
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading, autoScrollEnabled]);

  useEffect(() => {
    const container = messageContainerRef.current;
    if (!container) return;

    const handleScroll = () => {
      const currentlyHidden = Math.max(0, messages.length - Math.min(messages.length, visibleCount));
      const nearTop = container.scrollTop <= 40;
      const distanceFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight;
      const nearBottom = distanceFromBottom < 140;

      if (nearTop && currentlyHidden > 0 && !loadingOlderRef.current) {
        loadingOlderRef.current = true;
        restoreScrollRef.current = {
          previousHeight: container.scrollHeight,
          previousTop: container.scrollTop,
        };
        setVisibleCount((prev) => Math.min(messages.length, prev + LOAD_MORE_BATCH));
      }

      if (!userInteractedScrollRef.current) {
        setAutoScrollEnabled(true);
        setShowScrollToBottom(false);
        return;
      }

      setAutoScrollEnabled(nearBottom);
      setShowScrollToBottom(!nearBottom);
    };

    container.addEventListener("scroll", handleScroll, { passive: true });
    handleScroll();

    return () => container.removeEventListener("scroll", handleScroll);
  }, [messages.length, visibleCount]);

  useEffect(() => {
    if (messages.length <= INITIAL_VISIBLE_MESSAGES) {
      setVisibleCount(INITIAL_VISIBLE_MESSAGES);
      return;
    }

    setVisibleCount((prev) => {
      if (prev < INITIAL_VISIBLE_MESSAGES) return INITIAL_VISIBLE_MESSAGES;
      if (prev > messages.length) return messages.length;
      return prev;
    });
  }, [messages.length]);

  useEffect(() => {
    const element = textareaRef.current;
    if (!element) return;

    element.style.height = "auto";
    element.style.height = `${Math.min(element.scrollHeight, 220)}px`;
  }, [input]);

  useEffect(() => {
    const closeMenus = () => setMenuOpenId(null);
    window.addEventListener("click", closeMenus);
    return () => window.removeEventListener("click", closeMenus);
  }, []);

  useEffect(() => {
    return () => {
      if (activeRequestControllerRef.current) {
        activeRequestControllerRef.current.abort();
      }
    };
  }, []);

  const createConversationPreview = (content) => {
    const trimmed = content.trim();
    if (!trimmed) return "New chat";
    return trimmed.length > 40 ? `${trimmed.slice(0, 37)}...` : trimmed;
  };

  const modelNameForBackend = (value) => {
    if (value === "Groq") return "llama-3.3-70b-versatile";
    if (value === "Gemini") return "gemini-3-flash-preview";
    return "gemini-3-flash-preview";
  };

  const handleSend = useCallback(async () => {
    const trimmed = input.trim();
    if (!trimmed || loading) return;

    if (activeRequestControllerRef.current) {
      activeRequestControllerRef.current.abort();
    }

    const controller = new AbortController();
    activeRequestControllerRef.current = controller;

    const userMessage = {
      role: "user",
      content: trimmed,
      sentAt: new Date().toISOString(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setError("");
    setLoading(true);

    try {
      let response;
      const payload = {
        content: trimmed,
        modelName: modelNameForBackend(selectedModel),
      };

      if (currentConversationId == null) {
        response = await api.post("/api/messages", payload, {
          signal: controller.signal,
        });
        const newConversationId = response.data?.conversationId;
        setCurrentConversationId(newConversationId);

        setConversations((prev) =>
          sortConversationsByRecent([
            {
              id: newConversationId,
              title: createConversationPreview(trimmed),
              createdAt: new Date().toISOString(),
              lastUsedAt: new Date().toISOString(),
            },
            ...prev,
          ])
        );
        markConversationAsUsed(newConversationId, new Date().toISOString());
      } else {
        response = await api.post(`/api/messages/${currentConversationId}`, payload, {
          signal: controller.signal,
        });
      }

      const activeId = currentConversationId ?? response.data?.conversationId;
      markConversationAsUsed(activeId, new Date().toISOString());

      setMessages((prev) => [
        ...prev,
        {
          role: "assistant",
          content: response.data?.content || "",
          modelUsed: response.data?.modelUsed,
          providerUsed: response.data?.providerUsed,
          sentAt: response.data?.sentAt || new Date().toISOString(),
        },
      ]);
    } catch (requestError) {
      const wasCanceled =
        requestError?.name === "CanceledError" || requestError?.code === "ERR_CANCELED";

      if (wasCanceled) {
        setError("Generation stopped.");
      } else {
        setError("Something went wrong. Please try again.");
      }
    } finally {
      setLoading(false);
      activeRequestControllerRef.current = null;
    }
  }, [input, loading, selectedModel, currentConversationId, markConversationAsUsed]);

  const handleStopGenerating = useCallback(() => {
    if (!activeRequestControllerRef.current) return;
    activeRequestControllerRef.current.abort();
  }, []);

  const handleKeyDown = useCallback((event) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      handleSend();
    }
  }, [handleSend]);

  const handleNewChat = () => {
    setMessages([]);
    setCurrentConversationId(null);
    setError("");
    setSidebarOpen(false);
    setVisibleCount(INITIAL_VISIBLE_MESSAGES);
    userInteractedScrollRef.current = false;
    setAutoScrollEnabled(true);
    setShowScrollToBottom(false);
  };

  const handleScrollToBottom = useCallback(() => {
    userInteractedScrollRef.current = false;
    setAutoScrollEnabled(true);
    setShowScrollToBottom(false);
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, []);

  const handleSelectConversation = async (id) => {
    setCurrentConversationId(id);
    setSidebarOpen(false);
    setError("");
    setLoading(true);
    markConversationAsUsed(id, new Date().toISOString());

    try {
      const response = await api.get(`/api/conversations/${id}/messages`);
      const mapped = (response.data || []).map((item) => ({
        role: item.messageRole?.toLowerCase() || "assistant",
        content: item.content,
        modelUsed: item.modelUsed,
        providerUsed: item.providerUsed,
        sentAt: item.sentAt,
      }));
      setMessages(mapped);
      setVisibleCount(Math.min(INITIAL_VISIBLE_MESSAGES, mapped.length));
      userInteractedScrollRef.current = false;
      setAutoScrollEnabled(true);
      setShowScrollToBottom(false);
    } catch {
      setError("Failed to load conversation.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!loadingOlderRef.current) return;
    if (!restoreScrollRef.current) return;
    if (!messageContainerRef.current) return;

    const container = messageContainerRef.current;
    const { previousHeight, previousTop } = restoreScrollRef.current;
    const heightDiff = container.scrollHeight - previousHeight;
    container.scrollTop = previousTop + heightDiff;

    loadingOlderRef.current = false;
    restoreScrollRef.current = null;
  }, [visibleCount]);

  const handleRenameStart = (conversation) => {
    setRenameId(conversation.id);
    setRenameText(conversation.title || "");
    setMenuOpenId(null);
  };

  const handleRenameSave = () => {
    const title = renameText.trim();
    if (!title) {
      setRenameId(null);
      setRenameText("");
      return;
    }

    setConversations((prev) =>
      prev.map((item) => (item.id === renameId ? { ...item, title } : item))
    );

    setRenameId(null);
    setRenameText("");
  };

  const handleDeleteConversation = async (id) => {
    try {
      await api.delete(`/api/conversations/${id}`);

      setConversations((prev) => prev.filter((item) => item.id !== id));
      setMenuOpenId(null);
      saveConversationActivity((prev) => {
        const next = { ...prev };
        delete next[id];
        return next;
      });

      if (currentConversationId === id) {
        handleNewChat();
      }
    } catch (deleteError) {
      const backendMessage = deleteError?.response?.data?.message;
      setError(backendMessage || "Failed to delete conversation.");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/login");
  };

  const handleCopyMessage = useCallback(async (text, key) => {
    if (!text) return;

    try {
      await navigator.clipboard.writeText(text);
      setCopiedMessageKey(key);
      window.setTimeout(() => {
        setCopiedMessageKey((prev) => (prev === key ? "" : prev));
      }, 1400);
    } catch {
      setError("Unable to copy text.");
    }
  }, []);

  const visibleMessages = useMemo(() => {
    if (messages.length <= visibleCount) return messages;
    return messages.slice(messages.length - visibleCount);
  }, [messages, visibleCount]);

  const hiddenMessageCount = Math.max(0, messages.length - visibleMessages.length);

  useEffect(() => {
    if (!messageContainerRef.current) return;
    if (hiddenMessageCount !== 0) return;

    const container = messageContainerRef.current;
    const nearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 140;
    if (nearBottom) {
      bottomRef.current?.scrollIntoView({ behavior: "smooth" });
    }
  }, [hiddenMessageCount]);

  const renderedMessages = useMemo(
    () =>
      visibleMessages.map((message, idx) => {
        const messageKey = `${message.id || message.sentAt || "msg"}-${messages.length - visibleMessages.length + idx}`;

        return (
          <MessageItem
            key={messageKey}
            message={message}
            dark={dark}
            messageKey={messageKey}
            copiedMessageKey={copiedMessageKey}
            onCopyMessage={handleCopyMessage}
          />
        );
      }),
    [visibleMessages, dark, messages.length, copiedMessageKey, handleCopyMessage]
  );

  return (
    <div
      className={`relative flex h-screen ${
        dark ? "bg-[#212121] text-[#ececec]" : "bg-[#ffffff] text-[#171717]"
      }`}
    >
      {sidebarOpen && (
        <button
          aria-label="Close sidebar"
          onClick={() => setSidebarOpen(false)}
          className="fixed inset-0 z-20 bg-black/40 md:hidden"
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-30 flex w-72 flex-col transition-transform md:static md:translate-x-0 ${
          dark
            ? "border-r border-[#303030] bg-[#171717]"
            : "border-r border-[#e5e7eb] bg-[#f9fafb]"
        } ${
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="p-3">
          <button
            onClick={handleNewChat}
            className={`flex w-full items-center gap-2 rounded-lg border px-3 py-2 text-sm ${
              dark
                ? "border-[#303030] hover:bg-[#202123]"
                : "border-[#d1d5db] text-[#111827] hover:bg-[#f3f4f6]"
            }`}
          >
            <span className="text-base leading-none">+</span>
            <span>New Chat</span>
          </button>
        </div>

        <div className="flex-1 space-y-1 overflow-y-auto px-2 pb-3">
          {conversations.map((conversation) => {
            const isActive = currentConversationId === conversation.id;
            const isRenaming = renameId === conversation.id;

            return (
              <div
                key={conversation.id}
                className={`group rounded-lg px-2 py-2 ${
                  isActive
                    ? dark
                      ? "bg-[#202123]"
                      : "bg-[#eef2ff]"
                    : dark
                      ? "hover:bg-[#202123]"
                      : "hover:bg-[#f3f4f6]"
                }`}
              >
                {isRenaming ? (
                  <div className="space-y-2">
                    <input
                      value={renameText}
                      onChange={(e) => setRenameText(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === "Enter") handleRenameSave();
                        if (e.key === "Escape") {
                          setRenameId(null);
                          setRenameText("");
                        }
                      }}
                      className={`w-full rounded-md border px-2 py-1.5 text-sm outline-none ${
                        dark
                          ? "border-[#303030] bg-[#2f2f2f] text-[#ececec]"
                          : "border-[#d1d5db] bg-white text-[#111827]"
                      }`}
                      autoFocus
                    />
                    <div className="flex gap-2">
                      <button
                        onClick={handleRenameSave}
                        className={`rounded-md px-2 py-1 text-xs ${
                          dark
                            ? "bg-[#2f2f2f] text-[#ececec]"
                            : "bg-[#111827] text-white"
                        }`}
                      >
                        Save
                      </button>
                      <button
                        onClick={() => {
                          setRenameId(null);
                          setRenameText("");
                        }}
                        className={`rounded-md border px-2 py-1 text-xs ${
                          dark
                            ? "border-[#303030] text-[#b4b4b4]"
                            : "border-[#d1d5db] text-[#4b5563]"
                        }`}
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-start gap-2">
                    <button
                      onClick={() => handleSelectConversation(conversation.id)}
                      className="min-w-0 flex-1 text-left"
                    >
                      <p
                        className={`truncate text-sm ${
                          dark ? "text-[#ececec]" : "text-[#111827]"
                        }`}
                      >
                        {conversation.title || "New chat"}
                      </p>
                      <p
                        className={`mt-0.5 text-[11px] ${
                          dark ? "text-[#b4b4b4]" : "text-[#6b7280]"
                        }`}
                      >
                        {formatTime(conversation.createdAt)}
                      </p>
                    </button>

                    <div className="relative">
                      <button
                        onClick={(event) => {
                          event.stopPropagation();
                          setMenuOpenId((prev) => (prev === conversation.id ? null : conversation.id));
                        }}
                        className={`rounded-md p-1 opacity-0 transition group-hover:opacity-100 ${
                          dark
                            ? "text-[#b4b4b4] hover:bg-[#2f2f2f] hover:text-[#ececec]"
                            : "text-[#6b7280] hover:bg-[#e5e7eb] hover:text-[#111827]"
                        }`}
                      >
                        ...
                      </button>

                      {menuOpenId === conversation.id && (
                        <div
                          onClick={(event) => event.stopPropagation()}
                          className={`absolute right-0 top-8 z-20 w-28 rounded-md border p-1 shadow-lg ${
                            dark
                              ? "border-[#303030] bg-[#171717]"
                              : "border-[#d1d5db] bg-white"
                          }`}
                        >
                          <button
                            onClick={() => handleRenameStart(conversation)}
                            className={`flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs ${
                              dark
                                ? "text-[#ececec] hover:bg-[#202123]"
                                : "text-[#111827] hover:bg-[#f3f4f6]"
                            }`}
                          >
                            <span>✎</span>
                            <span>Rename</span>
                          </button>
                          <button
                            onClick={() => handleDeleteConversation(conversation.id)}
                            className={`flex w-full items-center gap-2 rounded px-2 py-1.5 text-xs ${
                              dark
                                ? "text-[#ececec] hover:bg-[#202123]"
                                : "text-[#111827] hover:bg-[#f3f4f6]"
                            }`}
                          >
                            <span>🗑</span>
                            <span>Delete</span>
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        <div className={`p-3 ${dark ? "border-t border-[#303030]" : "border-t border-[#e5e7eb]"}`}>
          <div
            className={`mb-2 flex items-center gap-2 rounded-lg px-3 py-2 ${
              dark ? "bg-[#202123]" : "bg-[#eef2ff]"
            }`}
          >
            <div
              className={`flex h-8 w-8 items-center justify-center rounded-full text-xs ${
                dark ? "bg-[#2f2f2f]" : "bg-white"
              }`}
            >
              {getInitials(userName)}
            </div>
            <div className="min-w-0">
              <p className={`truncate text-sm ${dark ? "text-[#ececec]" : "text-[#111827]"}`}>
                {userName}
              </p>
              <p className={`truncate text-xs ${dark ? "text-[#b4b4b4]" : "text-[#6b7280]"}`}>
                {userEmail}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={toggleTheme}
              className={`flex-1 rounded-lg border px-3 py-2 text-xs ${
                dark
                  ? "border-[#303030] text-[#b4b4b4] hover:bg-[#202123]"
                  : "border-[#d1d5db] text-[#4b5563] hover:bg-[#f3f4f6]"
              }`}
            >
              {theme === "dark" ? "Light Mode" : "Dark Mode"}
            </button>
            <button
              onClick={handleLogout}
              className={`rounded-lg border px-3 py-2 text-xs ${
                dark
                  ? "border-[#303030] text-[#b4b4b4] hover:bg-[#202123]"
                  : "border-[#d1d5db] text-[#4b5563] hover:bg-[#f3f4f6]"
              }`}
            >
              Logout
            </button>
          </div>
        </div>
      </aside>

      <main className={`flex min-w-0 flex-1 flex-col ${dark ? "bg-[#212121]" : "bg-white"}`}>
        <header
          className={`flex items-center justify-between px-4 py-3 md:px-6 ${
            dark ? "border-b border-[#303030]" : "border-b border-[#e5e7eb]"
          }`}
        >
          <div className="flex items-center gap-2">
            <button
              onClick={() => setSidebarOpen(true)}
              className={`rounded-md border px-2 py-1 text-xs md:hidden ${
                dark
                  ? "border-[#303030] text-[#b4b4b4]"
                  : "border-[#d1d5db] text-[#4b5563]"
              }`}
            >
              Menu
            </button>

            <select
              value={selectedModel}
              onChange={(e) => setSelectedModel(e.target.value)}
              className={`rounded-md border px-2 py-1.5 text-xs outline-none ${
                dark
                  ? "border-[#303030] bg-[#2f2f2f] text-[#ececec]"
                  : "border-[#d1d5db] bg-white text-[#111827]"
              }`}
            >
              {MODELS.map((model) => (
                <option key={model} value={model}>
                  {model}
                </option>
              ))}
            </select>
          </div>
        </header>

        <section
          ref={messageContainerRef}
          className="relative flex-1 overflow-y-auto"
          onWheel={() => {
            userInteractedScrollRef.current = true;
          }}
          onTouchMove={() => {
            userInteractedScrollRef.current = true;
          }}
          onMouseDown={() => {
            userInteractedScrollRef.current = true;
          }}
        >
          <div className="mx-auto flex w-full max-w-3xl flex-col gap-4 px-4 py-6 md:px-6">
            {messages.length === 0 && !loading && (
              <div className="mt-24 text-center">
                <p className={`text-lg font-semibold ${dark ? "text-[#ececec]" : "text-[#111827]"}`}>
                  How can I help today?
                </p>
              </div>
            )}

            {hiddenMessageCount > 0 && (
              <div className="text-center text-[11px] text-[#6b7280] dark:text-[#9ca3af]">
                Scroll up to load older messages ({hiddenMessageCount})
              </div>
            )}

            {renderedMessages}

            {loading && (
              <div className="max-w-[92%] rounded-xl px-4 py-3">
                <div
                  className={`mb-2 flex items-center gap-2 text-xs ${
                    dark ? "text-[#b4b4b4]" : "text-[#6b7280]"
                  }`}
                >
                  <span
                    className={`inline-flex h-5 w-5 items-center justify-center rounded-full text-[10px] ${
                      dark ? "bg-[#2f2f2f]" : "bg-[#e5e7eb]"
                    }`}
                  >
                    N
                  </span>
                  <span>Nurun</span>
                </div>
                <div className="flex items-center gap-1">
                  <span
                    className={`h-2 w-2 animate-bounce rounded-full [animation-delay:0ms] ${
                      dark ? "bg-[#b4b4b4]" : "bg-[#6b7280]"
                    }`}
                  />
                  <span
                    className={`h-2 w-2 animate-bounce rounded-full [animation-delay:150ms] ${
                      dark ? "bg-[#b4b4b4]" : "bg-[#6b7280]"
                    }`}
                  />
                  <span
                    className={`h-2 w-2 animate-bounce rounded-full [animation-delay:300ms] ${
                      dark ? "bg-[#b4b4b4]" : "bg-[#6b7280]"
                    }`}
                  />
                </div>
              </div>
            )}

            {error && (
              <p
                className={`rounded-lg border px-3 py-2 text-sm ${
                  dark
                    ? "border-[#303030] bg-[#171717] text-[#ececec]"
                    : "border-[#d1d5db] bg-[#f9fafb] text-[#111827]"
                }`}
              >
                {error}
              </p>
            )}

            <div ref={bottomRef} />
          </div>

          {showScrollToBottom && (
            <button
              type="button"
              onClick={handleScrollToBottom}
              className={`fixed bottom-28 right-6 z-20 rounded-full border px-3 py-2 text-xs shadow-lg md:right-10 ${
                dark
                  ? "border-[#303030] bg-[#171717] text-[#ececec] hover:bg-[#202123]"
                  : "border-[#d1d5db] bg-white text-[#111827] hover:bg-[#f3f4f6]"
              }`}
            >
              Go to bottom
            </button>
          )}
        </section>

        <footer
          className={`px-4 py-3 md:px-6 ${
            dark ? "border-t border-[#303030]" : "border-t border-[#e5e7eb]"
          }`}
        >
          <div className="mx-auto flex w-full max-w-3xl items-end gap-3">
            <textarea
              ref={textareaRef}
              rows={1}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Message Nurun"
              disabled={loading}
              className={`max-h-[220px] min-h-[50px] flex-1 resize-none overflow-y-auto rounded-2xl border px-4 py-3 text-sm outline-none ${
                dark
                  ? "border-[#303030] bg-[#2f2f2f] text-[#ececec] placeholder:text-[#b4b4b4]"
                  : "border-[#d1d5db] bg-white text-[#111827] placeholder:text-[#6b7280]"
              }`}
            />
            <button
              onClick={handleSend}
              disabled={loading || !input.trim()}
              className={`rounded-xl px-5 py-3 text-sm font-medium disabled:cursor-not-allowed disabled:opacity-40 ${
                dark
                  ? "bg-[#10a37f] text-white hover:bg-[#0e8f70]"
                  : "bg-[#111827] text-white hover:bg-[#1f2937]"
              }`}
            >
              Send
            </button>
            {loading && (
              <button
                type="button"
                onClick={handleStopGenerating}
                className={`rounded-xl border px-4 py-3 text-sm font-medium ${
                  dark
                    ? "border-[#3a3a3a] text-[#ececec] hover:bg-[#202123]"
                    : "border-[#d1d5db] text-[#111827] hover:bg-[#f3f4f6]"
                }`}
              >
                Stop
              </button>
            )}
          </div>
          <p
            className={`mx-auto mt-1 w-full max-w-3xl px-1 text-xs ${
              dark ? "text-[#b4b4b4]" : "text-[#6b7280]"
            }`}
          >
            Enter to send, Shift + Enter for newline
          </p>
        </footer>
      </main>
    </div>
  );
};

export default ChatPage;
