package network.ycc.waterdog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import network.ycc.waterdog.api.event.ServerChannelTapEvent;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.io.FileReader;
import java.util.function.Function;

public class WaterdogTools extends Plugin implements Listener {
    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, this);
    }

    @EventHandler
    public void onIntrospection(ServerChannelTapEvent event) {
        event.getChannel().pipeline().addLast(new IntrospectionInstance(event.getUser()));
    }

    public class IntrospectionInstance extends ChannelDuplexHandler implements Listener {
        final ProxiedPlayer user;
        ScriptEngine engine;
        Invocable invocable;
        boolean loadFailed;

        IntrospectionInstance(ProxiedPlayer user) {
            this.user = user;
            getProxy().getPluginManager().registerListener(WaterdogTools.this, this);
            reload();
        }

        void reload() {
            try {
                engine = new NashornScriptEngineFactory().getScriptEngine("--language=es6");
                invocable = (Invocable) engine;
                //stupid isolated class loaders. use this for classes in plugin(s)
                engine.put("PluginClass", (Function<String, Class>) s -> {
                    try {
                        return Class.forName(s);
                    } catch (ClassNotFoundException cnfe) {
                        throw new RuntimeException(cnfe);
                    }
                });
                engine.eval(new FileReader("script.js"));
                invocable.invokeFunction("init", user);
                loadFailed = false;
            } catch (Throwable e) {
                e.printStackTrace();
                loadFailed = true;
            }
        }

        @EventHandler
        public void onChat(ChatEvent event) {
            if(event.getSender() == user && event.getMessage().equals("/wdtreload")) {
                user.sendMessage("Reloading WaterdogTools scripts...");
                event.setCancelled(true);
                reload();
            }
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            getProxy().getPluginManager().unregisterListener(this);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            try {
                if (msg instanceof ByteBuf && !loadFailed) {
                    invocable.invokeFunction("write", ((ByteBuf) msg).retainedSlice());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            ctx.write(msg, promise);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                if (msg instanceof ByteBuf && !loadFailed) {
                    invocable.invokeFunction("read", ((ByteBuf) msg).retainedSlice());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            ctx.fireChannelRead(msg);
        }
    }
}
