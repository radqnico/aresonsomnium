def price(blocks, maxPrice):
    return int(blocks*maxPrice)

def getBlocks():
    a = 1.13
    r = []
    for i in range(0, 93):
        r.append(1500 * (a**i)) # 1-10 in f di x
    return r


prices = [
    1,
    2.5,
    2.5,
    6,
    6,
    11,
    11,
    25,
    25,
    55,
    55,
    55,
    90,
    170,
    170,
    290,
    290,
    480,
    800,
    800,
    800,
    1400,
    1400,
    2450,
    2450,
    3800,
    3800,
    8000,
    8000,
    18000,
    18000
    ]

totPrices = prices * 3

counter = 0
blocks = getBlocks()

for b in blocks:
    print(str(counter + 1) + ": " + str(price(b, totPrices[counter])))
    counter = counter + 1
