package com.cardshifter.api.messages;

import java.util.HashMap;
import java.util.Map;

import com.cardshifter.api.incoming.ChatMessage;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.api.incoming.RequestTargetsMessage;
import com.cardshifter.api.incoming.StartGameRequest;
import com.cardshifter.api.incoming.UseAbilityMessage;
import com.cardshifter.api.outgoing.AvailableTargetsMessage;
import com.cardshifter.api.outgoing.CardInfoMessage;
import com.cardshifter.api.outgoing.ClientDisconnectedMessage;
import com.cardshifter.api.outgoing.EntityRemoveMessage;
import com.cardshifter.api.outgoing.GameMessage;
import com.cardshifter.api.outgoing.NewGameMessage;
import com.cardshifter.api.outgoing.PlayerMessage;
import com.cardshifter.api.outgoing.ResetAvailableActionsMessage;
import com.cardshifter.api.outgoing.UpdateMessage;
import com.cardshifter.api.outgoing.UseableActionMessage;
import com.cardshifter.api.outgoing.WaitMessage;
import com.cardshifter.api.outgoing.WelcomeMessage;
import com.cardshifter.api.outgoing.ZoneChangeMessage;
import com.cardshifter.api.outgoing.ZoneMessage;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class MessageTypeIdResolver implements TypeIdResolver {
	
	private static final Map<String, Class<? extends Message>> clazzes = new HashMap<>();
	
	static {
		clazzes.put("chat", ChatMessage.class);
		clazzes.put("login", LoginMessage.class);
		clazzes.put("startgame", StartGameRequest.class);
		clazzes.put("use", UseAbilityMessage.class);
		clazzes.put("requestTargets", RequestTargetsMessage.class);
		clazzes.put("zoneChange", ZoneChangeMessage.class);
		clazzes.put("entityRemoved", EntityRemoveMessage.class);
		clazzes.put("disconnect", ClientDisconnectedMessage.class);
		
		clazzes.put("resetActions", ResetAvailableActionsMessage.class);
		clazzes.put("game", GameMessage.class);
		clazzes.put("wait", WaitMessage.class);
		clazzes.put("loginresponse", WelcomeMessage.class);
		clazzes.put("newgame", NewGameMessage.class);
		clazzes.put("player", PlayerMessage.class);
		clazzes.put("card", CardInfoMessage.class);
		clazzes.put("zone", ZoneMessage.class);
		clazzes.put("update", UpdateMessage.class);
		clazzes.put("useable", UseableActionMessage.class);
		clazzes.put("targets", AvailableTargetsMessage.class);
	}
	
	private JavaType mBaseType;

	@Override
	public void init(JavaType baseType) {
		mBaseType = baseType;
	}

	@Override
	public Id getMechanism() {
		return Id.CUSTOM;
	}

	@Override
	public String idFromValue(Object obj) {
		return idFromValueAndType(obj, obj.getClass());
	}

	@Override
	public String idFromBaseType() {
		// FindBugs: NullPointerException guaranteed when you pass in null
//		return idFromValueAndType(null, mBaseType.getRawClass());
		throw new AssertionError("this should never happen");
	}

	@Override
	public String idFromValueAndType(Object obj, Class<?> clazz) {
//		String name = clazz.getName();
//		if (name.startsWith(COMMAND_PACKAGE)) {
//			return name.substring(COMMAND_PACKAGE.length() + 1);
//		}
//		throw new IllegalStateException("class " + clazz
//				+ " is not in the package " + COMMAND_PACKAGE);
		Message mess = (Message) obj;
		return mess.getCommand();
	}

	@Override
	public JavaType typeFromId(String type) {
		Class<?> clazz = clazzes.get(type);
//		  String clazzName = COMMAND_PACKAGE + "." + type;
//		  try {
//			  clazz = ClassUtil.findClass(clazzName);
//		  } catch (ClassNotFoundException e) {
//			  throw new IllegalStateException("cannot find class '" + clazzName + "'");
//		  }
		if (clazz == null) {
			throw new UnsupportedOperationException("No such defined type: " + type);
		}
		return TypeFactory.defaultInstance().constructSpecializedType(mBaseType, clazz);
	}
}
