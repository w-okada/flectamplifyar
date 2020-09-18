package com.amplifyframework.datastore.generated.model;

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

/** This is an auto generated class representing the Marker type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Markers")
public final class Marker implements Model {
  public static final QueryField ID = field("id");
  public static final QueryField SCORE = field("score");
  public static final QueryField NAME = field("name");
  public static final QueryField PATH = field("path");
  public static final QueryField OWNER = field("owner");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="Int", isRequired = true) Integer score;
  private final @ModelField(targetType="String", isRequired = true) String name;
  private final @ModelField(targetType="String", isRequired = true) String path;
  private final @ModelField(targetType="String", isRequired = true) String owner;
  private final @ModelField(targetType="Canvas") @HasMany(associatedWith = "marker", type = Canvas.class) List<Canvas> canvases = null;
  public String getId() {
      return id;
  }
  
  public Integer getScore() {
      return score;
  }
  
  public String getName() {
      return name;
  }
  
  public String getPath() {
      return path;
  }
  
  public String getOwner() {
      return owner;
  }
  
  public List<Canvas> getCanvases() {
      return canvases;
  }
  
  private Marker(String id, Integer score, String name, String path, String owner) {
    this.id = id;
    this.score = score;
    this.name = name;
    this.path = path;
    this.owner = owner;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Marker marker = (Marker) obj;
      return ObjectsCompat.equals(getId(), marker.getId()) &&
              ObjectsCompat.equals(getScore(), marker.getScore()) &&
              ObjectsCompat.equals(getName(), marker.getName()) &&
              ObjectsCompat.equals(getPath(), marker.getPath()) &&
              ObjectsCompat.equals(getOwner(), marker.getOwner());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getScore())
      .append(getName())
      .append(getPath())
      .append(getOwner())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Marker {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("score=" + String.valueOf(getScore()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
      .append("path=" + String.valueOf(getPath()) + ", ")
      .append("owner=" + String.valueOf(getOwner()))
      .append("}")
      .toString();
  }
  
  public static ScoreStep builder() {
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
  public static Marker justId(String id) {
    try {
      UUID.fromString(id); // Check that ID is in the UUID format - if not an exception is thrown
    } catch (Exception exception) {
      throw new IllegalArgumentException(
              "Model IDs must be unique in the format of UUID. This method is for creating instances " +
              "of an existing object with only its ID field for sending as a mutation parameter. When " +
              "creating a new object, use the standard builder method and leave the ID field blank."
      );
    }
    return new Marker(
      id,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      score,
      name,
      path,
      owner);
  }
  public interface ScoreStep {
    NameStep score(Integer score);
  }
  

  public interface NameStep {
    PathStep name(String name);
  }
  

  public interface PathStep {
    OwnerStep path(String path);
  }
  

  public interface OwnerStep {
    BuildStep owner(String owner);
  }
  

  public interface BuildStep {
    Marker build();
    BuildStep id(String id) throws IllegalArgumentException;
  }
  

  public static class Builder implements ScoreStep, NameStep, PathStep, OwnerStep, BuildStep {
    private String id;
    private Integer score;
    private String name;
    private String path;
    private String owner;
    @Override
     public Marker build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Marker(
          id,
          score,
          name,
          path,
          owner);
    }
    
    @Override
     public NameStep score(Integer score) {
        Objects.requireNonNull(score);
        this.score = score;
        return this;
    }
    
    @Override
     public PathStep name(String name) {
        Objects.requireNonNull(name);
        this.name = name;
        return this;
    }
    
    @Override
     public OwnerStep path(String path) {
        Objects.requireNonNull(path);
        this.path = path;
        return this;
    }
    
    @Override
     public BuildStep owner(String owner) {
        Objects.requireNonNull(owner);
        this.owner = owner;
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
    private CopyOfBuilder(String id, Integer score, String name, String path, String owner) {
      super.id(id);
      super.score(score)
        .name(name)
        .path(path)
        .owner(owner);
    }
    
    @Override
     public CopyOfBuilder score(Integer score) {
      return (CopyOfBuilder) super.score(score);
    }
    
    @Override
     public CopyOfBuilder name(String name) {
      return (CopyOfBuilder) super.name(name);
    }
    
    @Override
     public CopyOfBuilder path(String path) {
      return (CopyOfBuilder) super.path(path);
    }
    
    @Override
     public CopyOfBuilder owner(String owner) {
      return (CopyOfBuilder) super.owner(owner);
    }
  }
  
}
