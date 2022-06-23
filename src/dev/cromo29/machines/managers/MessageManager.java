package dev.cromo29.machines.managers;

import com.google.common.collect.ImmutableMap;
import dev.cromo29.durkcore.util.TXT;
import dev.cromo29.machines.MachinePlugin;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MessageManager {

    private final MachinePlugin plugin;
    private final Map<String, String> messagesCache;

    public MessageManager(MachinePlugin plugin) {
        this.plugin = plugin;
        this.messagesCache = new HashMap<>();
    }

    /*
     * Manda uma mensagem da config com replaces.
     */
    public void sendMessage(CommandSender sender, String path, Object... replaces) {

        if (messagesCache.containsKey(path)) {
            sender.sendMessage(replace(messagesCache.get(path), replaces));
            return;
        }

        sender.sendMessage(getMessage(path, replaces));
    }

    /*
     * Manda uma mensagem da config.
     */
    public void sendMessage(CommandSender sender, String path) {

        if (messagesCache.containsKey(path)) {
            sender.sendMessage(messagesCache.get(path));
            return;
        }

        sender.sendMessage(getMessage(path));
    }

    /*
     * Pega uma mensagem da config com replaces.
     */
    public String getMessage(String path, Object... replaces) {

        if (messagesCache.containsKey(path)) return replace(messagesCache.get(path), replaces);

        return replace(getMessage(path), replaces);
    }

    /*
     * Pega uma mensagem da config com replaces.
     */
    public String getMessage(String path) {

        if (messagesCache.containsKey(path)) return messagesCache.get(path);

        String message = plugin.getMessagesFile().getString("Messages." + path);

        if (message != null) {
            message = message.replace("&", "§")
                    .replace("{prefix}", getPrefix());

            messagesCache.put(path, message);
        } else message = TXT.parse(getPrefix() + " <c>Mensagem não encontrada.");

        return message;
    }

    /*
     * Pega a prefixo configurada na config.
     */
    public String getPrefix() {
        if (messagesCache.containsKey("Messages.Prefix")) return messagesCache.get("Messages.Prefix");

        String prefix = plugin.getMessagesFile().getString("Messages.Prefix");

        if (prefix == null) prefix = "&8&l[&b29Chat&8&l]";

        prefix = prefix.replace("&", "§");

        messagesCache.put("Messages.Prefix", prefix);
        return prefix;
    }

    /*
     * Métodos para dar vários replaces de uma só vez.
     */
    private String replace(String text, Object... replace) {
        Iterator<Object> iter = Arrays.asList(replace).iterator();

        while (iter.hasNext()) {
            String key = iter.next() + "";
            String iterValue = iter.next() + "";

            text = text.replace(key, iterValue);
        }
        return text;
    }

    /*
     * Adicionar uma mensagem customizada com prefix.
     */
    public void putMessage(String path, String message, boolean prefix) {
        messagesCache.put(path, prefix ? getPrefix() + message : message);
    }

    /*
     * Adicionar uma mensagem customizada.
     */
    public void putMessage(String path, String message) {
        putMessage(path, message, false);
    }

    /*
     * Remove uma mensagem customizada.
     */
    public void removeMessage(String path) {
        messagesCache.remove(path);
    }

    /*
     * Pega todas as mensagens customizadas.
     */
    public Map<String, String> getMessagesCache() {
        return ImmutableMap.copyOf(messagesCache);
    }

    /*
     * Limpar cache das mensagens customizadas.
     */
    public void clearCache() {
        messagesCache.clear();
    }
}
