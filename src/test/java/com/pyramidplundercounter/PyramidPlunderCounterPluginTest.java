package com.pyramidplundercounter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PyramidPlunderCounterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PyramidPlunderCounterPlugin.class);
		RuneLite.main(args);
	}
}