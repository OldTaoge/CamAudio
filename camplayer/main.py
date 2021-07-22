"""Packages :
pynput
requests

"""

import requests
import time
import os

# import pynput


GET_URL = ""
KA_URL = ""
BEFORE_URL = ""

AUDIO_SERER_PATH = r"D:\OldTaogeCodes\camplayer\VolumeChanger.exe"


def get_local_time():
    return time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())


def log_info(s, l):
    l.write("[Info][" + time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) + "]" + s + "\n")
    l.flush()


def log_warn(s, l):
    l.write("[Warn][" + time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) + "]" + s + "\n")
    l.flush()


def log_err(s, l):
    l.write("[Err][" + time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()) + "]" + s + "\n")
    l.flush()


def init():
    res = requests.get(BEFORE_URL)
    with open("D:/tmp/before.mp4", "wb") as file:
        file.write(res.content)


def get_def():
    log_warn("Application loaded", log)
    set_volume()
    while True:
        try:
            res = requests.get(GET_URL)
            if res.text != "No file to download" and res.status_code == 200:
                log_info("Get new audio", log)
                with open("D:/tmp/dl.mp4", "wb") as file:
                    file.write(res.content)
                    log_info("Download audio", log)
                    set_volume()
                    os.system("ffplay -autoexit -i D:/tmp/before.mp4")
                    os.system("ffplay -autoexit -i D:/tmp/dl.mp4")
                    log_info("Played audio", log)
                os.remove("D:/tmp/dl.mp4")
            else:
                log_info("No Audio to play, sleep 5s", log)
                time.sleep(5)
            log_info("Keep alive info: " + requests.get(KA_URL).text, log)
        except Exception as e:
            log_err(str(e), log)
            time.sleep(5)


def set_volume():
    os.system(AUDIO_SERER_PATH)


if __name__ == '__main__':
    with open("D:/tmp/run.log", "w+") as log:
        init()
        get_def()
