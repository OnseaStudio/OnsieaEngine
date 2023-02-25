/**
*	Copyright 2021-2023 Onsea Studio All rights reserved.
*
*	This file is part of Onsiea Engine. (https://github.com/Onsea Studio/Onsea StudioEngine)
*
*	Unless noted in license (https://github.com/Onsea Studio/Onsea StudioEngine/blob/main/LICENSE.md) notice file (https://github.com/Onsea Studio/Onsea StudioEngine/blob/main/LICENSE_NOTICE.md), Onsea Studio engine and all parts herein is licensed under the terms of the LGPL-3.0 (https://www.gnu.org/licenses/lgpl-3.0.html)  found here https://www.gnu.org/licenses/lgpl-3.0.html and copied below the license file.
*
*	Onsiea Engine is libre software: you can redistribute it and/or modify
*	it under the terms of the GNU Lesser General Public License as published by
*	the Free Software Foundation, either version 3.0 of the License, or
*	(at your option) any later version.
*
*	Onsiea Engine is distributed in the hope that it will be useful,
*	but WITHOUT ANY WARRANTY; without even the implied warranty of
*	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*	GNU Lesser General Public License for more details.
*
*	You should have received a copy of the GNU Lesser General Public License
*	along with Onsiea Engine.  If not, see <https://www.gnu.org/licenses/> <https://www.gnu.org/licenses/lgpl-3.0.html>.
*
*	Neither the name "Onsea Studio", "Onsiea Engine", or any derivative name or the names of its authors / contributors may be used to endorse or promote products derived from this software and even less to name another project or other work without clear and precise permissions written in advance.
*
*	(more details on https://github.com/OnseaStudio/OnsieaEngine/wiki/License)
*
*	@author Seynax
*/
package fr.onsiea.engine.core.entity;

import org.joml.Vector3f;

import fr.onsiea.engine.client.graphics.opengl.hud.inventory.components.HotBar.OutHotBar;
import lombok.Getter;

/**
 * @Organization Onsea
 * @author Seynax
 *
 */
public class PlayerEntity extends Camera
{
	private @Getter OutHotBar hotBar;

	public PlayerEntity()
	{

	}

	public PlayerEntity(final Vector3f positionIn)
	{
		super(positionIn);
	}

	public PlayerEntity(final Vector3f positionIn, final Vector3f orientationIn)
	{
		super(positionIn, orientationIn);
	}

	/**
	 * @param hotbarIn
	 */
	public void hotBar(final OutHotBar hotBarIn)
	{
		this.hotBar = hotBarIn;
	}
}