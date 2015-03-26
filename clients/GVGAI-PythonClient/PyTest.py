from PyClient import PyClient
from EvoClient import EvoClient

# MOCK DATA PASSED TO THE CLIENT
def mockInit(isTraining):

    line = "INIT " + isTraining
    pyClient.processCommLine(line)
    pyClient.processLine(line)

    line = "Game#1.0#0#NO_WINNER#false#665#798#133#"
    pyClient.processLine(line)

    line = "Actions#ACTION_LEFT,ACTION_RIGHT,ACTION_DOWN,ACTION_UP,ACTION_NIL#"
    pyClient.processLine(line)

    line = "Avatar#266.0#133.0#0.0#0.0#1.0#ACTION_NIL#1,2;3,4#"
    pyClient.processLine(line)

    line = "s0#111111,101011,100001,110001,111111#"
    pyClient.processLine(line)

    line = "s1#000000,000000,010000,000000,000000#"
    pyClient.processLine(line)

    line = "s2#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s3#000000,000100,001110,001110,000000#"
    pyClient.processLine(line)

    line = "s4#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s5#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s6#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s7#000000,000000,000010,000000,000000#"
    pyClient.processLine(line)

    line = "s8#000000,010000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s9#000000,000000,000100,000100,000000#"
    pyClient.processLine(line)

    line = "s10#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "INIT-END 998"
    pyClient.processLine(line)

def mockAct(gameTick):


    line = "ACT"
    pyClient.processCommLine(line)
    pyClient.processLine(line)

    line = "Game#1.0#" + str(gameTick) + "#NO_WINNER#false#"
    pyClient.processLine(line)

    line = "Avatar#266.0#133.0#0.0#0.0#1.0#ACTION_NIL#1,2;3,4#"
    pyClient.processLine(line)

    line = "s0#111111,101011,100001,110001,111111#"
    pyClient.processLine(line)

    line = "s1#000000,000000,010000,000000,000000#"
    pyClient.processLine(line)

    line = "s2#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s3#000000,000100,001110,001110,000000#"
    pyClient.processLine(line)

    line = "s4#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s5#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s6#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s7#000000,000000,000010,000000,000000#"
    pyClient.processLine(line)

    line = "s8#000000,010000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s9#000000,000000,000100,000100,000000#"
    pyClient.processLine(line)

    line = "s10#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "ACT-END 38"
    pyClient.processCommLine(line)
    pyClient.processLine(line)


def mockEnd(gameTick):

    line = "ENDGAME"
    pyClient.processCommLine(line)
    pyClient.processLine(line)

    line = "Game#1.0#" + str(gameTick) + "#NO_WINNER#true#"
    pyClient.processLine(line)

    line = "Avatar#266.0#133.0#0.0#0.0#1.0#ACTION_NIL#1,2;3,4#"
    pyClient.processLine(line)

    line = "s0#111111,101011,100001,110001,111111#"
    pyClient.processLine(line)

    line = "s1#000000,000000,010000,000000,000000#"
    pyClient.processLine(line)

    line = "s2#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s3#000000,000100,001110,001110,000000#"
    pyClient.processLine(line)

    line = "s4#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s5#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s6#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s7#000000,000000,000010,000000,000000#"
    pyClient.processLine(line)

    line = "s8#000000,010000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "s9#000000,000000,000100,000100,000000#"
    pyClient.processLine(line)

    line = "s10#000000,000000,000000,000000,000000#"
    pyClient.processLine(line)

    line = "ENDGAME-END 38"
    pyClient.processLine(line)


pyClient = EvoClient()  #PyClient()
mockInit("true")

pyClient.game.printToFile(0)
pyClient.avatar.printToFile(0)

mockAct(1)
mockAct(2)
mockAct(3)
mockAct(4)

mockEnd(500)


mockInit("false")

pyClient.game.printToFile(1)
pyClient.avatar.printToFile(1)

mockAct(1)
mockAct(2)
mockAct(3)
mockAct(4)

mockEnd(500)

print "TEST FINISHED"