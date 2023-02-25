/**
 *
 */
package fr.onsiea.engine.client.graphics.opengl.hud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import fr.onsiea.engine.client.graphics.opengl.GraphicsUtils;
import fr.onsiea.engine.client.graphics.opengl.hud.components.HudComponent;
import fr.onsiea.engine.client.graphics.opengl.shaders.Shader2DIn3D;
import fr.onsiea.engine.client.graphics.opengl.shaders.Shader3DTo2D;
import fr.onsiea.engine.client.graphics.render.IRenderAPIContext;
import fr.onsiea.engine.client.graphics.window.IWindow;
import fr.onsiea.engine.client.input.InputManager;
import fr.onsiea.engine.utils.Pair;
import lombok.Getter;

/**
 * @author seyro
 *
 */
public class HudManager
{
	private final static Pair<Integer, Integer>	KEY_TO_CLOSE_ALL_HUDS	= new Pair<>(GLFW.GLFW_KEY_LEFT_CONTROL,
			GLFW.GLFW_KEY_ESCAPE);
	private final static int					KEY_TO_CLOSE_UPPER_HUDS	= GLFW.GLFW_KEY_ESCAPE;

	private final Map<String, Hud>				huds;

	private final Map<Integer, List<Hud>>		keyToReverseHuds;
	private final Map<Integer, List<Hud>>		keyToOpenHuds;
	private final Map<Integer, List<Hud>>		keyToCloseHuds;

	private final Map<String, Hud>				openedHuds;
	private final Map<String, Hud>				needFocusHuds;
	//private final List<Hud>						orderedOpenedHuds;

	private final List<HudComponent>			simpleHudComponents;

	private final Shader2DIn3D					shader2DIn3D;
	private final Shader3DTo2D					shader3Dto2D;

	private final IRenderAPIContext				renderAPIContext;

	private @Getter boolean						needFocus;

	public HudManager(final IRenderAPIContext renderAPIContextIn)
	{
		this.huds					= new HashMap<>();
		this.keyToReverseHuds		= new HashMap<>();
		this.keyToOpenHuds			= new HashMap<>();
		this.keyToCloseHuds			= new HashMap<>();
		this.openedHuds				= new LinkedHashMap<>();
		this.simpleHudComponents	= new ArrayList<>();
		this.needFocusHuds			= new LinkedHashMap<>();
		this.shader2DIn3D			= (Shader2DIn3D) renderAPIContextIn.shadersManager().get("2Din3D");
		this.shader3Dto2D			= (Shader3DTo2D) renderAPIContextIn.shadersManager().get("Shader3DTo2D");
		this.renderAPIContext		= renderAPIContextIn;
	}

	/**
	 * @param renderAPIContextIn
	 * @param hudCreativeInventoryIn
	 */
	public HudManager(final IRenderAPIContext renderAPIContextIn, final Hud... hudsIn)
	{
		this.huds					= new HashMap<>();
		this.keyToReverseHuds		= new HashMap<>();
		this.keyToOpenHuds			= new HashMap<>();
		this.keyToCloseHuds			= new HashMap<>();
		this.openedHuds				= new LinkedHashMap<>();
		this.simpleHudComponents	= new ArrayList<>();
		this.needFocusHuds			= new LinkedHashMap<>();
		this.shader2DIn3D			= (Shader2DIn3D) renderAPIContextIn.shadersManager().get("2Din3D");
		this.shader3Dto2D			= (Shader3DTo2D) renderAPIContextIn.shadersManager().get("Shader3DTo2D");
		this.renderAPIContext		= renderAPIContextIn;
		for (final Hud hud : hudsIn)
		{
			this.add(hud);
		}
	}

	public void update(final IWindow windowIn, final InputManager inputManagerIn)
	{
		if (inputManagerIn.keyboard().pressed().hasAll(HudManager.KEY_TO_CLOSE_ALL_HUDS.s1(),
				HudManager.KEY_TO_CLOSE_ALL_HUDS.s2()))
		{
			for (final Hud hud : this.openedHuds.values())
			{
				hud.close(inputManagerIn, windowIn);
			}
			this.openedHuds.clear();
		}
		else if (inputManagerIn.keyboard().pressed().has(HudManager.KEY_TO_CLOSE_UPPER_HUDS))
		{
		}

		List<Hud> huds = null;

		for (final int key : inputManagerIn.keyboard().justPressed().keys().keySet())
		{
			huds = this.keyToReverseHuds.get(key);

			if (huds != null)
			{
				this.reverseOpeningAll(huds, windowIn, inputManagerIn);
			}

			huds = this.keyToOpenHuds.get(key);

			if (huds != null)
			{
				this.openAll(huds, windowIn, inputManagerIn);
			}

			huds = this.keyToCloseHuds.get(key);

			if (huds != null)
			{
				this.closeAll(huds, windowIn, inputManagerIn);
			}
		}

		for (final Hud hud : this.openedHuds.values())
		{
			hud.update(windowIn, inputManagerIn);
		}
	}

	public void draw(final IWindow windowIn)
	{
		GraphicsUtils.prepare2D();
		this.shader2DIn3D.attach();

		for (final Hud hud : this.openedHuds.values())
		{
			hud.draw2D(this.shader2DIn3D);
		}

		for (final HudComponent component : this.simpleHudComponents)
		{
			component.draw2D(this.shader2DIn3D);
		}

		this.renderAPIContext.shadersManager().detach();
		GraphicsUtils.end2D();

		this.shader3Dto2D.attach();

		for (final Hud hud : this.openedHuds.values())
		{
			hud.draw3D(this.shader3Dto2D, windowIn);
		}

		for (final HudComponent component : this.simpleHudComponents)
		{
			component.draw3D(this.shader3Dto2D, windowIn);
		}

		this.renderAPIContext.shadersManager().detach();

		GL11.glScissor(0, 0, windowIn.effectiveWidth(), windowIn.effectiveHeight());
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glViewport(0, 0, windowIn.effectiveWidth(), windowIn.effectiveHeight());
	}

	public void add(final Hud hudIn)
	{
		this.huds.put(hudIn.name(), hudIn);
	}

	public void add(final String nameIn, final Hud hudIn)
	{
		this.huds.put(nameIn, hudIn);
	}

	public Hud get(final String nameIn)
	{
		return this.huds.get(nameIn);
	}

	public final void reverseOpeningAll(final Collection<Hud> hudsIn, final IWindow windowIn,
			final InputManager inputManagerIn)
	{
		for (final Hud hud : hudsIn)
		{
			hud.reverseOpening(inputManagerIn, windowIn);

			if (hud.isOpen())
			{
				this.openedHuds.put(hud.name(), hud);
				if (hud.needFocus())
				{
					this.needFocusHuds.put(hud.name(), hud);
					this.needFocus = true;
				}
			}
			else
			{
				this.openedHuds.remove(hud.name());
				if (hud.needFocus())
				{
					this.needFocusHuds.remove(hud.name());
					if (this.needFocusHuds.size() <= 0)
					{
						this.needFocus = false;
					}
				}
			}
		}
	}

	public final void openAll(final Collection<Hud> hudsIn, final IWindow windowIn, final InputManager inputManagerIn)
	{
		for (final Hud hud : hudsIn)
		{
			if (hud.isOpen())
			{
				continue;
			}

			hud.open(inputManagerIn, windowIn);
			this.openedHuds.put(hud.name(), hud);
			if (hud.needFocus())
			{
				this.needFocusHuds.put(hud.name(), hud);
				this.needFocus = true;
			}
		}
	}

	public final void closeAll(final Collection<Hud> hudsIn, final IWindow windowIn, final InputManager inputManagerIn)
	{
		for (final Hud hud : hudsIn)
		{
			if (hud.isOpen())
			{
				hud.close(inputManagerIn, windowIn);
				if (hud.needFocus())
				{
					this.needFocusHuds.remove(hud.name());
					if (this.needFocusHuds.size() <= 0)
					{
						this.needFocus = false;
					}
				}
				this.openedHuds.remove(hud.name());
			}
		}
	}

	public final void reverseOpeningAll(final IWindow windowIn, final InputManager inputManagerIn, final Hud... hudsIn)
	{
		for (final Hud hud : hudsIn)
		{
			hud.reverseOpening(inputManagerIn, windowIn);
			if (hud.isOpen())
			{
				this.openedHuds.put(hud.name(), hud);
				if (hud.needFocus())
				{
					this.needFocusHuds.put(hud.name(), hud);
					this.needFocus = true;
				}
			}
			else
			{
				this.openedHuds.remove(hud.name());
				if (hud.needFocus())
				{
					this.needFocusHuds.remove(hud.name());
					if (this.needFocusHuds.size() <= 0)
					{
						this.needFocus = true;
					}
				}
			}
		}
	}

	public final void openAll(final IWindow windowIn, final InputManager inputManagerIn, final Hud... hudsIn)
	{
		for (final Hud hud : hudsIn)
		{
			if (hud.isOpen())
			{
				continue;
			}

			hud.open(inputManagerIn, windowIn);
			this.openedHuds.put(hud.name(), hud);
			if (hud.needFocus())
			{
				this.needFocusHuds.put(hud.name(), hud);
				this.needFocus = true;
			}
		}
	}

	public final void closeAll(final IWindow windowIn, final InputManager inputManagerIn, final Hud... hudsIn)
	{
		for (final Hud hud : hudsIn)
		{
			if (hud.isOpen())
			{
				hud.close(inputManagerIn, windowIn);
				this.openedHuds.remove(hud.name());

				if (hud.needFocus())
				{
					this.needFocusHuds.remove(hud.name());
					if (this.needFocusHuds.size() <= 0)
					{
						this.needFocus = true;
					}
				}
			}
		}
	}

	public final void reverseOpening(final Hud hudIn, final IWindow windowIn, final InputManager inputManagerIn)
	{
		hudIn.reverseOpening(inputManagerIn, windowIn);
		if (hudIn.isOpen())
		{
			this.openedHuds.put(hudIn.name(), hudIn);
			if (hudIn.needFocus())
			{
				this.needFocusHuds.put(hudIn.name(), hudIn);
				this.needFocus = true;
			}
		}
		else
		{
			this.openedHuds.remove(hudIn.name());
			if (hudIn.needFocus())
			{
				this.needFocusHuds.remove(hudIn.name());
				if (this.needFocusHuds.size() <= 0)
				{
					this.needFocus = true;
				}
			}
		}
	}

	public final void open(final Hud hudIn, final IWindow windowIn, final InputManager inputManagerIn)
	{
		if (hudIn.isOpen())
		{
			return;
		}

		hudIn.open(inputManagerIn, windowIn);
		this.openedHuds.put(hudIn.name(), hudIn);
		if (hudIn.needFocus())
		{
			this.needFocusHuds.put(hudIn.name(), hudIn);
			this.needFocus = true;
		}
	}

	public final void close(final Hud hudIn, final IWindow windowIn, final InputManager inputManagerIn)
	{
		if (!hudIn.isOpen())
		{
			return;
		}

		hudIn.close(inputManagerIn, windowIn);
		this.openedHuds.remove(hudIn.name());
		if (hudIn.needFocus())
		{
			this.needFocusHuds.remove(hudIn.name());
			if (this.needFocusHuds.size() <= 0)
			{
				this.needFocus = true;
			}
		}
	}

	public boolean isOpen(final String hudIn)
	{
		final var hud = this.huds.get(hudIn);

		if (hud == null)
		{
			return false;
		}

		return hud.isOpen();
	}

	public void reverseOpening(final String hudIn, final IWindow windowIn, final InputManager inputManagerIn)
	{
		final var hud = this.huds.get(hudIn);

		if (hud == null)
		{
			return;
		}

		if (hud.isOpen())
		{
			this.open(hud, windowIn, inputManagerIn);
		}
		else
		{
			this.close(hud, windowIn, inputManagerIn);
		}
	}

	public void open(final String hudIn)
	{
		final var hud = this.huds.get(hudIn);

		if (hud == null)
		{
			return;
		}

		this.open(hudIn);
	}

	public void close(final String hudIn, final IWindow windowIn, final InputManager inputManagerIn)
	{
		final var hud = this.huds.get(hudIn);

		if (hud == null)
		{
			return;
		}

		this.close(hud, windowIn, inputManagerIn);
	}

	public void canReverseOpeningWith(final int keyIn, final Hud hudIn)
	{
		var huds = this.keyToReverseHuds.get(keyIn);

		if (huds == null)
		{
			huds = new ArrayList<>();

			this.keyToReverseHuds.put(keyIn, huds);
		}

		huds.add(hudIn);
	}

	public void canOpenWith(final int keyIn, final Hud hudIn)
	{
		var huds = this.keyToOpenHuds.get(keyIn);

		if (huds == null)
		{
			huds = new ArrayList<>();

			this.keyToReverseHuds.put(keyIn, huds);
		}

		huds.add(hudIn);
	}

	public void canCloseWith(final int keyIn, final Hud hudIn)
	{
		var huds = this.keyToCloseHuds.get(keyIn);

		if (huds == null)
		{
			huds = new ArrayList<>();

			this.keyToReverseHuds.put(keyIn, huds);
		}

		huds.add(hudIn);
	}

	public void removeReverseOpeningWith(final int keyIn, final Hud hudIn)
	{
		final var huds = this.keyToReverseHuds.get(keyIn);

		if (huds != null)
		{
			huds.remove(hudIn);
		}
	}

	public void removeOpeningWith(final int keyIn, final Hud hudIn)
	{
		final var huds = this.keyToOpenHuds.get(keyIn);

		if (huds != null)
		{
			huds.remove(hudIn);
		}
	}

	public void removeClosedWith(final int keyIn, final Hud hudIn)
	{
		final var huds = this.keyToCloseHuds.get(keyIn);

		if (huds != null)
		{
			huds.remove(hudIn);
		}
	}

	public void cleanup()
	{
		for (final var hud : this.huds.values())
		{
			hud.cleanup();
		}

		for (final var component : this.simpleHudComponents)
		{
			component.cleanup();
		}
	}
}