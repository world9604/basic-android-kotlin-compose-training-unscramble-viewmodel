package com.example.unscramble.ui.test

import com.example.unscramble.data.MAX_NO_OF_WORDS
import com.example.unscramble.data.SCORE_INCREASE
import com.example.unscramble.data.getUnscrambledWord
import com.example.unscramble.ui.GameViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * 요약
 * - testImplementation 구성을 사용하여 종속 항목이 애플리케이션 코드가 아닌 로컬 테스트 소스 코드에 적용됨을 나타냅니다.
 * - 테스트를 세 가지 시나리오(성공 경로, 오류 경로, 경계 사례)로 분류하는 것을 목표로 합니다.
 * - 좋은 단위 테스트에는 집중, 이해 가능, 확정성, 독립형이라는 적어도 4가지 특성이 있습니다.
 * - 테스트 메서드는 개별적으로 실행되어 변경 가능한 테스트 인스턴스 상태로 인한 예상치 못한 부작용을 방지합니다.
 * - 기본적으로 JUnit은 각 테스트 메서드가 실행되기 전에 테스트 클래스의 새 인스턴스를 만듭니다.
 * - 코드 적용 범위는 앱을 구성하는 클래스, 메서드, 코드 줄을 적절하게 테스트하는지 확인하는 데 핵심 역할을 합니다.
 */
/**
 * 다음 사항이 궁금할 수 있습니다.
 * 1. 동일한 viewModel 인스턴스가 모든 테스트에서 재사용된다는 의미인가요?
 * 2. 문제가 발생하지 않나요? 예를 들어 gameViewModel_CorrectWordGuessed_ScoreUpdatedAndErrorFlagUnset 테스트 메서드 다음에 gameViewModel_Initialization_FirstWordLoaded 테스트 메서드가 실행되면 어떻게 되나요? 초기화 테스트가 실패하나요?
 * 두 질문에 대한 답은 '아니요'입니다. 테스트 메서드는 개별적으로 실행되어 변경 가능한 테스트 인스턴스 상태로 인한 예상치 못한 부작용을 방지합니다. 기본적으로 JUnit은 각 테스트 메서드가 실행되기 전에 테스트 클래스의 새 인스턴스를 만듭니다.
 * 지금까지 GameViewModelTest 클래스에 4개의 테스트 메서드가 있으므로 GameViewModelTest는 4번 인스턴스화됩니다. 각 인스턴스에는 viewModel 속성의 자체 사본이 있습니다. 따라서 테스트 실행 순서는 중요하지 않습니다.
 * 참고: 이러한 '메서드별' 테스트 인스턴스 수명 주기는 JUnit4부터 기본 동작입니다.
 */
class GameViewModelTest {
    private val viewModel = GameViewModel()

    /**
     * 참고: 위 코드는 thingUnderTest_TriggerOfTest_ResultOfTest 형식을 사용하여 테스트 함수 이름을 지정합니다.
     * thingUnderTest = gameViewModel
     * TriggerOfTest = CorrectWordGuessed
     * ResultOfTest = ScoreUpdatedAndErrorFlagUnset
     */
    @Test
    fun gameViewModel_CorrectWordGuessed_ScoreUpdatedAndErrorFlagUnset() {
        var currentGameUiState = viewModel.uiState.value
        val correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)

        viewModel.updateUserGuess(correctPlayerWord)
        viewModel.checkUserGuess()

        currentGameUiState = viewModel.uiState.value
        // Assert that checkUserGuess() method updates isGuessedWordWrong is updated correctly.
        assertFalse(currentGameUiState.isGuessedWordWrong)
        // Assert that score is updated correctly.
        assertEquals(SCORE_AFTER_FIRST_CORRECT_ANSWER, currentGameUiState.score)
        /**
         * 경고: uiState를 가져오는 이 방법이 효과적인 이유는 개발자가 MutableStateFlow를 사용했기 때문입니다.
         * 다음 단원에서는 데이터 스트림을 만드는 StateFlow의 고급 사용법을 알아보며
         * 개발자는 스트림을 처리하기 위해 반응해야 합니다.
         * 이러한 시나리오에서는 다양한 메서드와 접근 방식을 사용하여 단위 테스트를 작성합니다.
         */
    }

    companion object {
        private const val SCORE_AFTER_FIRST_CORRECT_ANSWER = SCORE_INCREASE
    }

    @Test
    fun gameViewModel_IncorrectGuess_ErrorFlagSet() {
        // Given an incorrect word as input
        val incorrectPlayerWord = "and"

        viewModel.updateUserGuess(incorrectPlayerWord)
        viewModel.checkUserGuess()

        val currentGameUiState = viewModel.uiState.value
        // Assert that score is unchanged
        assertEquals(0, currentGameUiState.score)
        // Assert that checkUserGuess() method updates isGuessedWordWrong correctly
        assertTrue(currentGameUiState.isGuessedWordWrong)
    }

    @Test
    fun gameViewModel_Initialization_FirstWordLoaded() {
        val gameUiState = viewModel.uiState.value
        val unScrambledWord = getUnscrambledWord(gameUiState.currentScrambledWord)

        // Assert that current word is scrambled.
        assertNotEquals(unScrambledWord, gameUiState.currentScrambledWord)
        // Assert that current word count is set to 1.
        assertTrue(gameUiState.currentWordCount == 1)
        // Assert that initially the score is 0.
        assertTrue(gameUiState.score == 0)
        // Assert that the wrong word guessed is false.
        assertFalse(gameUiState.isGuessedWordWrong)
        // Assert that game is not over.
        assertFalse(gameUiState.isGameOver)
    }

    @Test
    fun gameViewModel_AllWordsGuessed_UiStateUpdatedCorrectly() {
        var expectedScore = 0
        var currentGameUiState = viewModel.uiState.value
        var correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)
        repeat(MAX_NO_OF_WORDS) {
            expectedScore += SCORE_INCREASE
            viewModel.updateUserGuess(correctPlayerWord)
            viewModel.checkUserGuess()
            currentGameUiState = viewModel.uiState.value
            correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)
            // Assert that after each correct answer, score is updated correctly.
            assertEquals(expectedScore, currentGameUiState.score)
        }
        // Assert that after all questions are answered, the current word count is up-to-date.
        assertEquals(MAX_NO_OF_WORDS, currentGameUiState.currentWordCount)
        // Assert that after 10 questions are answered, the game is over.
        assertTrue(currentGameUiState.isGameOver)
    }

    /**
     *  Run With Coverage
     *  - 밝은 녹색은 이러한 코드 줄이 포함됐음을 나타냅니다.
     *  - 옅은 분홍색으로 표시된 두 줄을 확인할 수 있습니다. 이 색상은 이러한 코드 줄에 단위 테스트가 적용되지 않았음을 나타냅니다.
     *
     *  적용 범위를 개선하려면 누락된 경로를 포함하는 테스트를 작성해야 합니다.
     */
    @Test
    fun gameViewModel_WordSkipped_ScoreUnchangedAndWordCountIncreased() {
        var currentGameUiState = viewModel.uiState.value
        val correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)
        viewModel.updateUserGuess(correctPlayerWord)
        viewModel.checkUserGuess()

        currentGameUiState = viewModel.uiState.value
        val lastWordCount = currentGameUiState.currentWordCount
        viewModel.skipWord()
        currentGameUiState = viewModel.uiState.value
        // Assert that score remains unchanged after word is skipped.
        assertEquals(SCORE_AFTER_FIRST_CORRECT_ANSWER, currentGameUiState.score)
        // Assert that word count is increased by 1 after word is skipped.
        assertEquals(lastWordCount + 1, currentGameUiState.currentWordCount)
    }
}