import difflib
from difflib import SequenceMatcher
from typing import List, Dict, Tuple, Optional, Union
from abc import ABC, abstractmethod

try:
    from sklearn.feature_extraction.text import TfidfVectorizer
    from sklearn.metrics.pairwise import cosine_similarity
    SKLEARN_AVAILABLE = True
except ImportError:
    SKLEARN_AVAILABLE = False
    print("Warning: scikit-learn not available. N-gram similarity will use fallback method.")


class BaseSimilarity(ABC):
    """유사도 계산 기본 추상 클래스"""

    @abstractmethod
    def calculate(self, word1: str, word2: str) -> float:
        """두 단어 간의 유사도를 계산합니다 (0~1 범위)"""
        pass


class LevenshteinSimilarity(BaseSimilarity):
    """편집 거리(Levenshtein Distance) 기반 유사도 계산"""

    def _levenshtein_distance(self, s1: str, s2: str) -> int:
        """편집 거리 계산"""
        if len(s1) < len(s2):
            return self._levenshtein_distance(s2, s1)

        if len(s2) == 0:
            return len(s1)

        previous_row = list(range(len(s2) + 1))
        for i, c1 in enumerate(s1):
            current_row = [i + 1]
            for j, c2 in enumerate(s2):
                insertions = previous_row[j + 1] + 1
                deletions = current_row[j] + 1
                substitutions = previous_row[j] + (c1 != c2)
                current_row.append(min(insertions, deletions, substitutions))
            previous_row = current_row

        return previous_row[-1]

    def calculate(self, word1: str, word2: str) -> float:
        """편집 거리 기반 유사도 계산 (0~1)"""
        if not word1 or not word2:
            return 0.0 if word1 != word2 else 1.0

        distance = self._levenshtein_distance(word1, word2)
        max_len = max(len(word1), len(word2))

        if max_len == 0:
            return 1.0

        return 1 - (distance / max_len)


class DifflibSimilarity(BaseSimilarity):
    """difflib을 사용한 유사도 계산"""

    def calculate(self, word1: str, word2: str) -> float:
        """difflib 기반 유사도 계산"""
        return SequenceMatcher(None, word1, word2).ratio()


class JamoSimilarity(BaseSimilarity):
    """자모 분해 기반 한국어 단어 유사도"""

    def __init__(self):
        # 초성, 중성, 종성 테이블
        self.cho_list = ['ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ']
        self.jung_list = ['ㅏ','ㅐ','ㅑ','ㅒ','ㅓ','ㅔ','ㅕ','ㅖ','ㅗ','ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ']
        self.jong_list = ['','ㄱ','ㄲ','ㄱㅅ','ㄴ','ㄴㅈ','ㄴㅎ','ㄷ','ㄹ','ㄹㄱ','ㄹㅁ','ㄹㅂ','ㄹㅅ','ㄹㅌ','ㄹㅍ','ㄹㅎ','ㅁ','ㅂ','ㅂㅅ','ㅅ','ㅆ','ㅇ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ']

        # 편집거리 계산기 초기화
        self.levenshtein = LevenshteinSimilarity()

    def _decompose_hangul(self, char: str) -> str:
        """한글 문자를 자모로 분해"""
        if '가' <= char <= '힣':
            code = ord(char) - ord('가')
            jong = code % 28
            jung = (code - jong) // 28 % 21
            cho = (code - jong - jung * 28) // 28 // 21

            return self.cho_list[cho] + self.jung_list[jung] + self.jong_list[jong]
        else:
            return char

    def calculate(self, word1: str, word2: str) -> float:
        """자모 분해 기반 유사도 계산"""
        # 각 문자를 자모로 분해
        jamo1 = ''.join([self._decompose_hangul(char) for char in word1])
        jamo2 = ''.join([self._decompose_hangul(char) for char in word2])

        # 자모 기준으로 편집 거리 계산
        return self.levenshtein.calculate(jamo1, jamo2)


class NGramSimilarity(BaseSimilarity):
    """N-gram 기반 유사도 계산"""

    def __init__(self, ngram_range: Tuple[int, int] = (2, 3)):
        self.ngram_range = ngram_range

    def _get_ngrams(self, text: str, n: int) -> set:
        """N-gram 생성"""
        if len(text) < n:
            return {text}
        return set([text[i:i+n] for i in range(len(text)-n+1)])

    def _ngram_similarity_fallback(self, word1: str, word2: str, n: int = 2) -> float:
        """N-gram 유사도 계산 (scikit-learn 없을 때 사용)"""
        ngrams1 = self._get_ngrams(word1, n)
        ngrams2 = self._get_ngrams(word2, n)

        if not ngrams1 and not ngrams2:
            return 1.0

        intersection = len(ngrams1.intersection(ngrams2))
        union = len(ngrams1.union(ngrams2))

        return intersection / union if union > 0 else 0.0

    def calculate(self, word1: str, word2: str) -> float:
        """N-gram 기반 유사도 계산"""
        if word1 == word2:
            return 1.0

        if not word1 or not word2:
            return 0.0

        if SKLEARN_AVAILABLE:
            try:
                # TF-IDF 벡터화 (문자 n-gram 사용)
                vectorizer = TfidfVectorizer(
                    analyzer='char_wb',  # 단어 경계 기준 문자 n-gram
                    ngram_range=self.ngram_range,
                    lowercase=False
                )

                vectors = vectorizer.fit_transform([word1, word2])
                similarity_matrix = cosine_similarity(vectors)
                return similarity_matrix[0, 1]
            except:
                pass

        # scikit-learn이 없거나 오류 발생 시 fallback
        return self._ngram_similarity_fallback(word1, word2, self.ngram_range[0])


class JaccardSimilarity(BaseSimilarity):
    """Jaccard 계수 기반 n-gram 유사도"""

    def __init__(self, n: int = 2):
        self.n = n

    def _get_ngrams(self, text: str, n: int) -> set:
        """N-gram 생성"""
        if len(text) < n:
            return {text}
        return set([text[i:i+n] for i in range(len(text)-n+1)])

    def calculate(self, word1: str, word2: str) -> float:
        """Jaccard 계수 기반 유사도 계산"""
        ngrams1 = self._get_ngrams(word1, self.n)
        ngrams2 = self._get_ngrams(word2, self.n)

        if not ngrams1 and not ngrams2:
            return 1.0

        intersection = len(ngrams1.intersection(ngrams2))
        union = len(ngrams1.union(ngrams2))

        return intersection / union if union > 0 else 0.0
