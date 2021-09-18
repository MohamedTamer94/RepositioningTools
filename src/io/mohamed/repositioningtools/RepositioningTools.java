package io.mohamed.repositioningtools;

import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class RepositioningTools extends AndroidNonvisibleComponent implements Component {

  // the registered components and their IDs
  private final HashMap<String, RegisteredComponent> components = new HashMap<>();
  // the elevation value
  private float elevation = 20;
  // the component default elevation value
  private float defaultElevation;
  // the component x - the raw component x
  private float dX = 0;
  // the component y - the raw component y
  private float dY = 0;
  // the component x + ( the component x - raw x )
  private float x = 0;
  // the component y + ( the component y - raw y )
  private float y = 0;
  // weather to accept drags without calling the AcceptDrag function or not
  private boolean acceptDrags = true;

  /**
   * Creates a new Repositioning Tools component
   *
   * @param container the container for the component to be placed in
   */
  public RepositioningTools(final ComponentContainer container) {
    super(container.$form());
  }

  /**
   * Unregisters the component specified. You will no longer get drag feedback for the given
   * component.
   *
   * @param id the component id to unregister
   */
  @SimpleFunction(description = "Unregisters the component specified. You will no longer get drag feedback for the given component.")
  public void UnregisterComponent(String id) {
    AndroidViewComponent component = components.get(id).getComponent();
    View view = getView(component);
    if (view == null) {
      return; // should never happen!
    }
    view.setOnTouchListener(null); // remove the touch listener.
    components.remove(id);
  }

  /**
   * Registers the component specified so it could be dragged by the user.
   *
   * @param component  the component to register
   * @param horizontal weather to allow dragging horizontally
   * @param vertical   weather to allow dragging vertically
   * @param id         a unique identifier for the component
   */
  @SimpleFunction(description = "Registers the component specified so it could be dragged by the user.")
  public void RegisterComponent(final AndroidViewComponent component, final boolean horizontal,
      final boolean vertical, String id) {
    try {
      if (components.containsKey(id)) {
        UnregisterComponent(id);
      }
      components.put(id, new RegisteredComponent(component, vertical, horizontal));
      View view = getView(component);
      if (view != null) {
        view.setOnTouchListener((v, event) -> {
          final View view1 = component.getView();
          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
              view.getParent().requestDisallowInterceptTouchEvent(true);
              defaultElevation = view1.getZ();
              dX = view1.getX() - event.getRawX();
              dY = view1.getY() - event.getRawY();
              break;
            case MotionEvent.ACTION_UP:
              view.getParent().requestDisallowInterceptTouchEvent(false);
              x = event.getRawX() + dX;
              y = event.getRawY() + dY;
              Dragged(component, id, x, y, event);
              view.setZ(defaultElevation);
              break;
            case MotionEvent.ACTION_MOVE:
              x = event.getRawX() + dX;
              y = event.getRawY() + dY;
              if (acceptDrags) {
                AcceptDrag(id);
              }
              Dragging(component, id, x, y, event);
              break;
          }
          v.invalidate();
          return true;
        });
      }
    } catch (Exception e) {
      Error(e.getMessage());
    }
  }

  /**
   * Accepts drag for the given id. This block is useful to allow drags only if some conditions were
   * met.
   *
   * @param id the id
   */
  @SimpleFunction(description = "Accepts drag for the given id. This block is useful to allow drags only if some conditions were met.")
  public void AcceptDrag(String id) {
    View view = getView(components.get(id).getComponent());
    boolean vertical = components.get(id).isVertical();
    boolean horizontal = components.get(id).isHorizontal();
    if (view != null) {
      view.setZ(elevation);
      if (vertical && horizontal) {
        view.animate()
            .x(x)
            .y(y)
            .setDuration(0)
            .start();
      } else if (horizontal) {
        view.animate()
            .x(x)
            .setDuration(0)
            .start();
      } else if (vertical) {
        view.animate()
            .y(y)
            .setDuration(0)
            .start();
      }
    }
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_FLOAT, defaultValue = "20")
  @SimpleProperty(description = "Sets the shadow elevation when the component is dragged.")
  public void ShadowElevation(float value) {
    this.elevation = value;
  }

  @SimpleProperty(description = "Returns the elevation shadow.")
  public float ShadowElevation() {
    return elevation;
  }

  /**
   * If true, drags will always be accepted without using the AcceptDrag block.
   *
   * @param acceptDrags true to always accept drags
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
  @SimpleProperty
  public void AlwaysAcceptDrags(boolean acceptDrags) {
    this.acceptDrags = acceptDrags;
  }

  /**
   * @return true to accept drags without calling the AcceptDrag block.
   */
  @SimpleProperty
  public boolean AlwaysAcceptDrags() {
    return this.acceptDrags;
  }

  @SimpleEvent(description = "Called when the component is dragged by the user.")
  public void Dragged(AndroidViewComponent component, String id, float x, float y, Object event) {
    EventDispatcher.dispatchEvent(this, "Dragged", component, id, x, y, event);
  }

  @SimpleEvent(description = "Called when the component is getting dragged by the user.")
  public void Dragging(AndroidViewComponent component, String id, float x, float y, Object event) {
    EventDispatcher.dispatchEvent(this, "Dragging", component, id, x, y, event);
  }

  @SimpleEvent(description = "Called when an error occurs.")
  public void Error(String error) {
    EventDispatcher.dispatchEvent(this, "Error", error);
  }

  /**
   * Uses reflections to find the `getViewHelper` method for kodular card view
   *
   * @param methods the methods to search in
   * @return the method found, or null if there isn't
   */
  private Method findViewHelperMethod(Method[] methods) {
    for (Method method : methods) {
      if ((method.getName().equals("getViewHelper".trim())) && (method.getParameterTypes().length
          == 0)) {
        return method;
      }
    }
    return null;
  }

  /**
   * Drags the specified component to the given location.
   *
   * @param component the component to drag
   * @param x         the X coordinate for the new location for the component
   * @param y         the Y coordinate for the new location for the component
   * @param duration  the duration of the movement
   */
  @SimpleFunction(description = "Drags the specified component to the given location.")
  public void DragComponent(AndroidViewComponent component, float x, float y, int duration) {
    View view = component.getView();
    view.animate()
        .y(y)
        .x(x)
        .setDuration(duration)
        .start();
  }

  /**
   * Returns the view for the specified {@link AndroidViewComponent}
   *
   * @param component the component to return the {@link View}
   * @return the view for the {@link AndroidViewComponent}
   */
  @Nullable
  private View getView(@NonNull AndroidViewComponent component) {
    final String componentName = component.getClass().getSimpleName();
    // a hack to workaround the bug that the actual view returned by the getView() method isn't draggable
    if (componentName.equals("MakeroidCardView")) {
      Method method = findViewHelperMethod(component.getClass().getMethods());
      if (method != null) {
        try {
          return (View) method.invoke(component, new Object[]{});
        } catch (IllegalAccessException | InvocationTargetException e) {
          e.printStackTrace();
        }
      }
    } else {
      return component.getView();
    }
    return null;
  }

  /**
   * A class to represent the component the user registers
   */
  private static class RegisteredComponent {

    // the registered AndroidViewComponent
    private final AndroidViewComponent component;
    // weather to allow dragging vertically
    private final boolean vertical;
    // weather to allow dragging horizontally
    private final boolean horizontal;

    /**
     * Creates a new RegisteredComponent
     *
     * @param component  the AndroidViewComponent to register
     * @param vertical   weather to allow dragging vertically
     * @param horizontal weather to allow dragging horizontally
     */
    public RegisteredComponent(AndroidViewComponent component, boolean vertical,
        boolean horizontal) {
      this.component = component;
      this.horizontal = horizontal;
      this.vertical = vertical;
    }

    /**
     * @return true to allow dragging horizontally
     */
    public boolean isHorizontal() {
      return horizontal;
    }

    /**
     * @return true to allow dragging vertically
     */
    public boolean isVertical() {
      return vertical;
    }

    /**
     * @return the android view component registered
     */
    public AndroidViewComponent getComponent() {
      return component;
    }
  }
}
