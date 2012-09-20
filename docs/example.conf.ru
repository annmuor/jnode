# Конфигурационный файл jNode 0.3.4
# Комментарии начинаются с '#'
# Название узла
info.stationname = Sample Node
# Где находится
info.location = The City Of Country
# Кто сисоп
info.sysop = Rodriguez bender
# NDL - флаги нодлиста
info.ndl = 115200,TCP,BINKP
# FTN-адрес узла
info.address = 2:256/128@fidonet
# Данные для коннекта к базе
jdbc.url = jdbc:postgresql://localhost/database
jdbc.user = username
jdbc.pass = password
# Что слушать для входящий соединений
binkp.bind = 0.0.0.0
# Какой порт слушать
binkp.port = 24554
# Папка для приёма файлов
binkp.inbound = inbound
# Закомментируйте если хотите выключить сервер
binkp.server = 1
# Закомментируйте если хотите выключить клиент
binkp.client = 1
# Задержка перед первым пуллом после стартапа
poll.delay = 600
# Периоды ожидания между пулами ( после первого пула )
poll.period = 600
# Логлевел, от 5 до 1
log.level = 4
# Путь к нодлисту ( только читать )
nodelist.path = NODELIST
# Путь к индексу нодлиста ( читать и писать )
nodelist.index = NODELIST.idx
