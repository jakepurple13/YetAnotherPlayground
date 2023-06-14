package com.programmersbox.testing.poker

enum class PokerHand(
    val rank: Int,
    val initialWinning: Int,
) {
    RoyalFlush(9, 250) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            if (h[1].value == 10) {
                if (h[2].value == 11) {
                    if (h[3].value == 12) {
                        if (h[4].value == 13) {
                            if (h[0].value == 1) {
                                if (Straight.check(h) && Flush.check(h)) {
                                    return true
                                }
                            }
                        }
                    }
                }
            }
            return false
        }
    },
    StraightFlush(8, 50) {
        override fun check(hand: List<Card>): Boolean = Straight.check(hand) && Flush.check(hand)
    },
    FourOfAKind(7, 25) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var acceptable = false
            var count = 0
            val numberCount = h[3].value
            for (element in h) {
                if (element.value == numberCount) {
                    count++
                }
            }
            if (count == 4) {
                acceptable = true
            }

            return acceptable
        }
    },
    FullHouse(6, 9) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var count = 1
            var found = false
            var found1 = false
            for (i in 1 until h.size) {
                if (h[i].compareTo(h[i - 1]) == 0) {
                    count++
                } else {
                    if (count == 3) {
                        found1 = true
                    } else if (count == 2) {
                        found = true
                    }
                    count = 1
                }

            }

            if (count == 3) {
                found1 = true
            } else if (count == 2) {
                found = true
            }

            return found && found1
        }
    },
    Flush(5, 6) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            for (i in 1 until h.size) {
                if (h[i].suit != h[i - 1].suit) {
                    return false
                }
            }
            return true
        }
    },
    Straight(4, 4) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var count = 0
            var value: Int
            for (i in 0 until h.size - 1) {
                value = h[i].value
                if (value == 1) {
                    if (h[i + 1].value == 2) {
                        value = 1
                    } else if (h[i + 1].value == 10) {
                        value = 9
                    }
                }
                if (value + 1 == h[i + 1].value) {
                    count++
                }
            }

            return count == 4
        }
    },
    ThreeOfAKind(3, 3) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var acceptable = false
            var count = 1
            var hold = false
            for (i in 1 until h.size) {

                if (h[i].compareTo(h[i - 1]) == 0) {
                    count++
                    hold = true
                } else if (hold) {
                    break
                }

            }

            if (count == 3) {
                acceptable = true
            }

            return acceptable
        }
    },
    TwoPair(2, 2) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var count = 1
            var found = false
            var found1 = false
            var i = 1
            while (i < h.size) {
                if (h[i].compareTo(h[i - 1]) == 0) {
                    count++
                    i++
                }
                if (count == 2 && found1) {
                    found = true
                    count = 1
                } else if (count == 2) {
                    found1 = true
                    count = 1
                }
                i++


            }

            if (count == 2) {
                found1 = true
            } else if (count == 2 && found1) {
                found = true
            }

            return found && found1
        }
    },
    Pair(1, 1) {
        override fun check(hand: List<Card>): Boolean {
            val h = hand.sortedBy { it.value }
            var acceptable = false
            var count = 0
            for (i in 1 until h.size) {
                //if (h[i].compareTo(h[i - 1]) == 0) {
                val valueMin = if (jacksOrBetter) 11 else 1
                if (h[i].compareTo(h[i - 1]) == 0 && h[i].value > valueMin || h[i].value == 1) {
                    count++
                }

            }

            if (count == 1) {
                acceptable = true
            }

            return acceptable
        }
    },
    HighCard(0, 0) {
        override fun check(hand: List<Card>): Boolean = true
    };

    abstract fun check(hand: List<Card>): Boolean

    val shortenedName
        get() = when (this) {
            RoyalFlush -> "Royal"
            StraightFlush -> "SF"
            FourOfAKind -> "4K"
            FullHouse -> "FullH"
            Flush -> "Flush"
            Straight -> "Straight"
            ThreeOfAKind -> "3K"
            TwoPair -> "2P"
            Pair -> "Pair"
            HighCard -> "High"
        }

    companion object {
        var jacksOrBetter = false
    }
}