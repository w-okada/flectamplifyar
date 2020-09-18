package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.annotations.BelongsTo;
import com.amplifyframework.core.model.annotations.HasMany;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Canvas type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Canvas")
@Index(name = "byMarker", fields = {"markerID"})
public final class Canvas implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField TITLE = field("title");
  public static final QueryField OWNER = field("owner");
  public static final QueryField MARKER = field("markerID");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String title;
  private final @ModelField(targetType="String", isRequired = true) String owner;
  private final @ModelField(targetType="Marker") @BelongsTo(targetName = "markerID", type = Marker.class) Marker marker;
  private final @ModelField(targetType="Element") @HasMany(associatedWith = "canvas", type = Element.class) List<Element> elements = null;
  public String getId() {
      return id;
  }
  
  public String getTitle() {
      return title;
  }
  
  public String getOwner() {
      return owner;
  }
  
  public Marker getMarker() {
      return marker;
  }
  
  public List<Element> getElements() {
      return elements;
  }
  
  private Canvas(String id, String title, String owner, Marker marker) {
    this.id = id;
    this.title = title;
    this.owner = owner;
    this.marker = marker;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Canvas canvas = (Canvas) obj;
      return ObjectsCompat.equals(getId(), canvas.getId()) &&
              ObjectsCompat.equals(getTitle(), canvas.getTitle()) &&
              ObjectsCompat.equals(getOwner(), canvas.getOwner()) &&
              ObjectsCompat.equals(getMarker(), canvas.getMarker());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getTitle())
      .append(getOwner())
      .append(getMarker())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Canvas {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("title=" + String.valueOf(getTitle()) + ", ")
      .append("owner=" + String.valueOf(getOwner()) + ", ")
      .append("marker=" + String.valueOf(getMarker()))
      .append("}")
      .toString();
  }
  
  public static TitleStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   * @throws IllegalArgumentException Checks that ID is in the proper format
   */
  public static Canvas justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Canvas(
      id,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      title,
      owner,
      marker);
  }
  public interface TitleStep {
    OwnerStep title(String title);
  }
  

  public interface OwnerStep {
    BuildStep owner(String owner);
  }
  

  public interface BuildStep {
    Canvas build();
    BuildStep id(String id) throws IllegalArgumentException;
    BuildStep marker(Marker marker);
  }
  

  public static class Builder implements TitleStep, OwnerStep, BuildStep {
    private String id;
    private String title;
    private String owner;
    private Marker marker;
    @Override
     public Canvas build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Canvas(
          id,
          title,
          owner,
          marker);
    }
    
    @Override
     public OwnerStep title(String title) {
        Objects.requireNonNull(title);
        this.title = title;
        return this;
    }
    
    @Override
     public BuildStep owner(String owner) {
        Objects.requireNonNull(owner);
        this.owner = owner;
        return this;
    }
    
    @Override
     public BuildStep marker(Marker marker) {
        this.marker = marker;
        return this;
    }
    
    /** 
     * WARNING: Do not set ID when creating a new object. Leave this blank and one will be auto generated for you.
     * This should only be set when referring to an already existing object.
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     * @throws IllegalArgumentException Checks that ID is in the proper format
     */
    public BuildStep id(String id) throws IllegalArgumentException {
        this.id = id;
        
        try {
            UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
        } catch (Exception exception) {
          throw new IllegalArgumentException("Model IDs must be unique in the format of UUID.",
                    exception);
        }
        
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String title, String owner, Marker marker) {
      super.id(id);
      super.title(title)
        .owner(owner)
        .marker(marker);
    }
    
    @Override
     public CopyOfBuilder title(String title) {
      return (CopyOfBuilder) super.title(title);
    }
    
    @Override
     public CopyOfBuilder owner(String owner) {
      return (CopyOfBuilder) super.owner(owner);
    }
    
    @Override
     public CopyOfBuilder marker(Marker marker) {
      return (CopyOfBuilder) super.marker(marker);
    }
  }
  
}
