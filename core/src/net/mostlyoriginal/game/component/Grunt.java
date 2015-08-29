package net.mostlyoriginal.game.component;

import com.artemis.EntityFactory;
import com.artemis.annotations.Bind;

@Bind({Position.class, Size.class})
public interface Grunt extends EntityFactory<Grunt>{
  Grunt position(float x, float y);
  Grunt size(float width, float height);
}