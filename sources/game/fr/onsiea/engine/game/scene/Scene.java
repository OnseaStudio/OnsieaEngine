/**
* Copyright 2021 Onsiea All rights reserved.<br><br>
*
* This file is part of Onsiea Engine project. (https://github.com/Onsiea/OnsieaEngine)<br><br>
*
* Onsiea Engine is [licensed] (https://github.com/Onsiea/OnsieaEngine/blob/main/LICENSE) under the terms of the "GNU General Public Lesser License v3.0" (GPL-3.0).
* https://github.com/Onsiea/OnsieaEngine/wiki/License#license-and-copyright<br><br>
*
* Onsiea Engine is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3.0 of the License, or
* (at your option) any later version.<br><br>
*
* Onsiea Engine is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.<br><br>
*
* You should have received a copy of the GNU Lesser General Public License
* along with Onsiea Engine.  If not, see <https://www.gnu.org/licenses/>.<br><br>
*
* Neither the name "Onsiea", "Onsiea Engine", or any derivative name or the names of its authors / contributors may be used to endorse or promote products derived from this software and even less to name another project or other work without clear and precise permissions written in advance.<br><br>
*
* @Author : Seynax (https://github.com/seynax)<br>
* @Organization : Onsiea Studio (https://github.com/Onsiea)
*/
package fr.onsiea.engine.game.scene;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joml.Vector3f;

import fr.onsiea.engine.client.graphics.fog.Fog;
import fr.onsiea.engine.client.graphics.light.DirectionalLight;
import fr.onsiea.engine.client.graphics.opengl.flare.FlareManager;
import fr.onsiea.engine.client.graphics.opengl.hud.HudManager;
import fr.onsiea.engine.client.graphics.opengl.hud.inventory.components.HotBar;
import fr.onsiea.engine.client.graphics.particles.ParticlesManager;
import fr.onsiea.engine.client.graphics.render.IRenderAPIContext;
import fr.onsiea.engine.client.graphics.window.IWindow;
import fr.onsiea.engine.client.input.InputManager;
import fr.onsiea.engine.core.entity.PlayerEntity;
import fr.onsiea.engine.game.GameTest;
import fr.onsiea.engine.game.scene.item.SceneItems;
import fr.onsiea.engine.game.scene.light.SceneLights;
import fr.onsiea.engine.game.world.World;
import fr.onsiea.engine.utils.time.Timer;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author Seynax
 *
 */
@Getter(AccessLevel.PUBLIC)
public class Scene
{
	public final static int							MAX_LIGHTS	= 4;

	private final SceneLights						sceneLights;
	private SceneItems								sceneItems;
	private final Fog								fog;
	private final PlayerEntity						playerEntity;

	private float									lightAngle;
	private final float								angleInc;

	// World

	private final World								world;

	private final SceneRenderer						sceneRenderer;

	// Sun

	private final Vector3f							sunPosition;
	private final boolean							hasSun;

	// Particles

	private final Map<String, ParticlesManager<?>>	particlesManagers;

	// Timers

	private final Timer								inputTimer;

	public Scene(final IRenderAPIContext contextIn, final IWindow windowIn, final float clearColorRIn,
			final float clearColorGIn, final float clearColorBIn, final DirectionalLight directionalLightIn,
			final float specularPowerIn, final Vector3f ambientLightIn, final Fog fogIn, final HotBar hotBarIn,
			final FlareManager flareManagerIn) throws Exception
	{
		this.sceneLights				= new SceneLights(directionalLightIn, specularPowerIn, ambientLightIn);
		//this.sceneItems				= new SceneItems();
		this.fog						= fogIn;
		this.playerEntity				= new PlayerEntity();
		this.playerEntity.position().z	= 2.0f;

		this.lightAngle					= 45.0f;
		this.angleInc					= 0.0f;

		// World

		this.world						= new World(contextIn.shadersManager(), contextIn, this.playerEntity(),
				windowIn);

		this.sceneRenderer				= new SceneRenderer(contextIn, windowIn, clearColorRIn, clearColorGIn,
				clearColorBIn, fogIn, this.playerEntity, flareManagerIn, null, ambientLightIn, this.world);

		// Sun

		this.sunPosition				= new Vector3f();
		this.hasSun						= false;

		// Particles

		this.particlesManagers			= new LinkedHashMap<>();

		// Timers

		this.inputTimer					= new Timer();
	}

	// Tests

	public static boolean depthMode = false;

	public final Scene input(final IWindow windowIn, final InputManager inputManagerIn, final HudManager hudManagerIn)
	{
		if (!hudManagerIn.needFocus())
		{
			inputManagerIn.shortcuts().setContext("GENERAL");
			if (this.inputTimer.isTime(1_000_000_0L))
			{
				this.playerEntity.input(windowIn, inputManagerIn);
				/**if (inputManagerIn.glfwGetKey(GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS)
				{
					this.angleInc -= 0.05f;
				}
				else if (inputManagerIn.glfwGetKey(GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS)
				{
					this.angleInc += 0.05f;
				}
				else
				{
					this.angleInc = 0;
				}**/

				// Tests

				if (inputManagerIn.shortcuts().isEnabled("DEPTH_MODE"))
				{
					GameTest.loggers.logLn("DEPTH MODE !");
					Scene.depthMode = true;
				}
				if (inputManagerIn.shortcuts().isEnabled("COLOR_MODE"))
				{
					GameTest.loggers.logLn("COLOR MODE !");
					Scene.depthMode = false;
				}
			}
		}
		else
		{
			inputManagerIn.shortcuts().setContext("HUDS");
			this.playerEntity.resetIndicators();
		}

		return this;
	}

	public final Scene update()
	{
		return this;
	}

	public final Scene render(final IWindow windowIn)
	{
		this.lightAngle += this.angleInc;
		if (this.lightAngle < -90)
		{
			this.lightAngle = -90;
		}
		else if (this.lightAngle > 90)
		{
			this.lightAngle = 90;
		}

		final var	zValue			= (float) Math.cos(Math.toRadians(this.lightAngle));
		final var	yValue			= (float) Math.sin(Math.toRadians(this.lightAngle));

		final var	lightDirection	= this.sceneLights.directionalLight().direction();
		lightDirection.x	= 0;
		lightDirection.y	= yValue;
		lightDirection.z	= zValue;
		lightDirection.normalize();

		this.sceneRenderer.render(this, windowIn, false);

		return this;
	}

	public final void cleanup()
	{
		this.sceneRenderer().cleanup();
	}
}