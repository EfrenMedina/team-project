package entity;

import org.json.JSONObject;

/**
 * Deliberately breaks the architecture rules (an entity using org.json) to prove CI fails.
 * SCRUM-24 red-check only: never merge.
 */
public class ArchitectureRedCheck {

  /**
   * Returns an empty JSON object.
   *
   * @return an empty JSON object
   */
  public JSONObject json() {
    return new JSONObject();
  }
}
