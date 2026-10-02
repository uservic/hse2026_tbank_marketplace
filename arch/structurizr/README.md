# Локальный запуск Structurizr
- установить Docker
- скачать образ 
  - `-docker pull structurizr/structurizr`
- запустить контейнер, связав локальную папку с рабочей директорией Structurizr
  - Powershell
    - `docker run -it --rm -p 8080:8080 -v "C:\c4_projects\marketplace:/usr/local/structurizr" structurizr/structurizr local`
- код для отрисовки схем в файле workspace.dsl
