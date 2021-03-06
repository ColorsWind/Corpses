package me.virizion.corpses.nms.nmsclasses.packetlisteners;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Field;

import me.virizion.corpses.ConfigData;
import me.virizion.corpses.Main;
import me.virizion.corpses.nms.Corpses.CorpseData;
import net.minecraft.server.v1_8_R2.NetworkManager;
import net.minecraft.server.v1_8_R2.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R2.PacketPlayInUseEntity.EnumEntityUseAction;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PcktIn_v1_8_R2 extends ChannelInboundHandlerAdapter {

	private Player p;

	public PcktIn_v1_8_R2(Player p) {
		this.p = p;
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof PacketPlayInUseEntity) {
			final PacketPlayInUseEntity packet = (PacketPlayInUseEntity) msg;
			Bukkit.getServer().getScheduler()
					.runTask(Main.getPlugin(), new Runnable() {
						public void run() {
							if (ConfigData.hasLootingInventory()) {
								if (packet.a() == EnumEntityUseAction.INTERACT_AT) {
									for (CorpseData cd : Main.getPlugin().corpses
											.getAllCorpses()) {
										if (cd.getEntityId() == getId(packet)) {
											p.openInventory(cd
													.getLootInventory());
											break;
										}
									}
								}
							}
						}
					});
		}
		super.channelRead(ctx, msg);
	}

	private int getId(PacketPlayInUseEntity packet) {
		try {
			Field afield = packet.getClass().getDeclaredField("a");
			afield.setAccessible(true);
			int id = afield.getInt(packet);
			afield.setAccessible(false);
			return id;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static final void registerListener(Player p) {
		Channel c = getChannel(p);
		if (c == null) {
			throw new NullPointerException("Couldn't get channel??");
		}
		c.pipeline().addBefore("packet_handler", "packet_in_listener",
				new PcktIn_v1_8_R2(p));
	}

	public static final Channel getChannel(Player p) {
		NetworkManager nm = ((CraftPlayer) p).getHandle().playerConnection.networkManager;
		try {
			Field ifield = nm.getClass().getDeclaredField("k");
			ifield.setAccessible(true);
			Channel c = (Channel) ifield.get(nm);
			ifield.setAccessible(false);
			return c;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
