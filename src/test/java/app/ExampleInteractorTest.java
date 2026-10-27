package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Template for testing a use case interactor. Copy this pattern for real interactors.
 *
 * <ul>
 *   <li>In-memory DAO instead of the database, so tests are fast and need no files.
 *   <li>Fake presenter that records what the interactor sent, instead of a real UI.
 *   <li>Fixed {@link Clock}, so anything time-based gives the same answer every run.
 *   <li>Test names describe behaviour: {@code <action>_<condition>_<expected result>}.
 * </ul>
 *
 * <p>The tiny "save a note" use case below exists only to show the pattern. Real interactors
 * live in {@code src/main/java}; only their test doubles belong in the test.
 */
class ExampleInteractorTest {

  private static final Instant NOW = Instant.parse("2026-10-27T14:30:00Z");

  private InMemoryNoteDataAccess dataAccess;
  private FakeNotePresenter presenter;
  private SaveNoteInteractor interactor;

  @BeforeEach
  void setUp() {
    dataAccess = new InMemoryNoteDataAccess();
    presenter = new FakeNotePresenter();
    Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
    interactor = new SaveNoteInteractor(dataAccess, presenter, fixedClock);
  }

  @Test
  void saveNote_validText_storesNoteAndPresentsSuccess() {
    interactor.execute(new SaveNoteInputData("n1", "Buy AAPL under $150"));

    assertEquals("Buy AAPL under $150", dataAccess.notes.get("n1").text());
    assertEquals(NOW, dataAccess.notes.get("n1").savedAt());
    assertEquals(NOW, presenter.success.savedAt());
    assertNull(presenter.error);
  }

  @Test
  void saveNote_blankText_presentsErrorAndStoresNothing() {
    interactor.execute(new SaveNoteInputData("n1", "   "));

    assertEquals("Note text cannot be empty.", presenter.error);
    assertNull(presenter.success);
    assertTrue(dataAccess.notes.isEmpty());
  }

  // ---- Example use case (in real code these are separate files in src/main/java) ----

  record Note(String id, String text, Instant savedAt) {}

  record SaveNoteInputData(String id, String text) {}

  record SaveNoteOutputData(String id, Instant savedAt) {}

  interface NoteDataAccessInterface {
    void save(Note note);
  }

  interface SaveNoteOutputBoundary {
    void prepareSuccessView(SaveNoteOutputData outputData);

    void prepareFailView(String error);
  }

  static class SaveNoteInteractor {
    private final NoteDataAccessInterface dataAccess;
    private final SaveNoteOutputBoundary presenter;
    private final Clock clock;

    SaveNoteInteractor(
        NoteDataAccessInterface dataAccess, SaveNoteOutputBoundary presenter, Clock clock) {
      this.dataAccess = dataAccess;
      this.presenter = presenter;
      this.clock = clock;
    }

    void execute(SaveNoteInputData input) {
      if (input.text().isBlank()) {
        presenter.prepareFailView("Note text cannot be empty.");
        return;
      }
      Note note = new Note(input.id(), input.text(), clock.instant());
      dataAccess.save(note);
      presenter.prepareSuccessView(new SaveNoteOutputData(note.id(), note.savedAt()));
    }
  }

  // ---- Test doubles ----

  /** Stores notes in a map instead of SQLite. */
  static class InMemoryNoteDataAccess implements NoteDataAccessInterface {
    final Map<String, Note> notes = new HashMap<>();

    @Override
    public void save(Note note) {
      notes.put(note.id(), note);
    }
  }

  /** Records the last thing the interactor presented, so tests can assert on it. */
  static class FakeNotePresenter implements SaveNoteOutputBoundary {
    SaveNoteOutputData success;
    String error;

    @Override
    public void prepareSuccessView(SaveNoteOutputData outputData) {
      success = outputData;
    }

    @Override
    public void prepareFailView(String error) {
      this.error = error;
    }
  }
}
