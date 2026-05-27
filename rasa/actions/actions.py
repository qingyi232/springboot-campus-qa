from typing import Any, Text, Dict, List
from rasa_sdk import Action, Tracker, FormValidationAction
from rasa_sdk.executor import CollectingDispatcher
from rasa_sdk.types import DomainDict
from rasa_sdk.events import SlotSet, AllSlotsReset, EventType
import requests
import json
import re

# SpringBoot后端API地址
API_BASE_URL = "http://localhost:8080/api"


class ActionAnswerQuestion(Action):
    """从语料库中查找答案"""

    def name(self) -> Text:
        return "action_answer_question"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        user_message = tracker.latest_message.get('text')
        
        try:
            # 直接调用后端的语料库搜索API（避免死循环）
            response = requests.get(
                f"{API_BASE_URL}/qa-corpus/search",
                params={"question": user_message},
                headers={"Content-Type": "application/json"},
                timeout=5  # 5秒超时
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('code') == 200 and data.get('data'):
                    corpus = data.get('data')
                    answer = corpus.get('answer', '抱歉，我没有找到相关答案。')
                    # 清除Markdown格式符号
                    answer = self.clean_markdown(answer)
                    dispatcher.utter_message(text=answer)
                else:
                    # 语料库未找到，返回默认消息
                    dispatcher.utter_message(text="抱歉，我暂时无法回答这个问题。您可以尝试换个方式提问。")
            else:
                dispatcher.utter_message(text="抱歉，系统暂时不可用，请稍后再试。")
                
        except requests.exceptions.Timeout:
            print(f"Timeout searching corpus for: {user_message}")
            dispatcher.utter_message(text="查询超时，请稍后再试。")
        except Exception as e:
            print(f"Error calling backend API: {e}")
            dispatcher.utter_message(text="抱歉，系统出现错误，请稍后再试。")

        return []
    
    def clean_markdown(self, text: str) -> str:
        """清除Markdown格式符号"""
        if not text:
            return text
        # 移除粗体标记 **text**
        text = re.sub(r'\*\*([^*]+)\*\*', r'\1', text)
        # 移除单个星号 *text*
        text = re.sub(r'\*([^*]+)\*', r'\1', text)
        # 移除其他常见Markdown符号
        text = text.replace('###', '').replace('##', '').replace('#', '')
        return text


class ActionSearchContent(Action):
    """搜索内容管理模块"""

    def name(self) -> Text:
        return "action_search_content"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        user_message = tracker.latest_message.get('text')
        
        try:
            # 调用后端API搜索内容
            response = requests.get(
                f"{API_BASE_URL}/content/page",
                params={
                    "current": 1,
                    "size": 5,
                    "keyword": user_message,
                    "status": 1
                }
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('code') == 200:
                    records = data.get('data', {}).get('records', [])
                    if records:
                        # 只取第一条最相关的内容，返回完整详细信息
                        item = records[0]
                        title = item.get('title', '')
                        content = item.get('content', '')
                        author = item.get('author', '')
                        category = item.get('category', '')
                        
                        # 返回完整的内容
                        message = f"📚 {title}\n\n"
                        if content:
                            message += f"{content}\n\n"
                        if author:
                            message += f"📝 发布者: {author}\n"
                        if category:
                            message += f"🏷️ 分类: {category}\n"
                        
                        dispatcher.utter_message(text=message)
                    else:
                        dispatcher.utter_message(text="没有找到相关内容，请换个关键词试试。")
                else:
                    dispatcher.utter_message(text="查询失败，请稍后再试。")
            else:
                dispatcher.utter_message(text="系统暂时不可用，请稍后再试。")
                
        except Exception as e:
            print(f"Error searching content: {e}")
            dispatcher.utter_message(text="抱歉，查询出现错误。")

        return []


class ActionSearchActivity(Action):
    """搜索活动信息（动态搜索数据库）"""

    def name(self) -> Text:
        return "action_search_activity"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        user_message = tracker.latest_message.get('text')
        
        try:
            # 直接使用用户输入作为关键词（无需预定义关键词列表，支持搜索任意新增内容）
            params = {
                "current": 1,
                "size": 5,
                "status": 1,
                "keyword": user_message  # 直接用用户问题搜索，后端会自动模糊匹配
            }
            
            response = requests.get(
                f"{API_BASE_URL}/activity/page",
                params=params
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('code') == 200:
                    records = data.get('data', {}).get('records', [])
                    if records:
                        # 只取第一条最相关的活动，返回完整详细信息
                        item = records[0]
                        title = item.get('title', '')
                        description = item.get('description', '')
                        location = item.get('location', '')
                        start_time = item.get('startTime', '')
                        end_time = item.get('endTime', '')
                        registration_deadline = item.get('registrationDeadline', '')
                        requirements = item.get('requirements', '')
                        organizer = item.get('organizer', '')
                        current_participants = item.get('currentParticipants', 0)
                        max_participants = item.get('maxParticipants', 0)
                        
                        # 返回完整的活动详情
                        message = f"🎯 {title}\n\n"
                        if description:
                            message += f"{description}\n\n"
                        if location:
                            message += f"📍 地点: {location}\n"
                        if start_time:
                            message += f"⏰ 开始时间: {start_time}\n"
                        if end_time:
                            message += f"⏰ 结束时间: {end_time}\n"
                        if registration_deadline:
                            message += f"📅 报名截止: {registration_deadline}\n"
                        if max_participants:
                            message += f"👥 报名情况: {current_participants}/{max_participants}人\n"
                        if requirements:
                            message += f"\n📋 参加要求:\n{requirements}\n"
                        if organizer:
                            message += f"\n🏛️ 主办方: {organizer}\n"
                        
                        message += "\n💡 提示：点击「活动中心」可报名参加"
                        dispatcher.utter_message(text=message)
                    else:
                        dispatcher.utter_message(text="未找到相关活动，请尝试其他关键词。")
                else:
                    dispatcher.utter_message(text="查询失败，请稍后再试。")
            else:
                dispatcher.utter_message(text="系统暂时不可用，请稍后再试。")
                
        except Exception as e:
            print(f"Error searching activities: {e}")
            dispatcher.utter_message(text="抱歉，查询出现错误。")

        return []


class ActionSearchNews(Action):
    """搜索校园资讯"""

    def name(self) -> Text:
        return "action_search_news"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        user_message = tracker.latest_message.get('text')
        
        try:
            # 直接使用用户输入作为关键词（无需预定义关键词列表，支持搜索任意新增内容）
            params = {
                "current": 1,
                "size": 5,
                "status": 1,
                "keyword": user_message  # 直接用用户问题搜索，后端会自动模糊匹配
            }
            
            response = requests.get(
                f"{API_BASE_URL}/news/page",
                params=params
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('code') == 200:
                    records = data.get('data', {}).get('records', [])
                    if records:
                        # 只取第一条最相关的资讯
                        item = records[0]
                        title = item.get('title', '')
                        content = item.get('content', '')
                        author = item.get('author', '')
                        publish_time = item.get('publishTime', '')
                        
                        # 调试日志（强制刷新）
                        import sys
                        print(f"\n{'='*50}", flush=True)
                        print(f"DEBUG ActionSearchNews:", flush=True)
                        print(f"  title: {title}", flush=True)
                        print(f"  content type: {type(content)}", flush=True)
                        print(f"  content is None: {content is None}", flush=True)
                        print(f"  content length: {len(content) if content else 0}", flush=True)
                        if content:
                            print(f"  content preview: {content[:100]}...", flush=True)
                        print(f"  author: {author}", flush=True)
                        print(f"  publishTime: {publish_time}", flush=True)
                        print(f"{'='*50}\n", flush=True)
                        
                        # 返回完整的资讯内容
                        message = f"📰 {title}\n\n"
                        if content:
                            message += f"{content}\n\n"
                        if author:
                            message += f"📝 发布单位: {author}\n"
                        if publish_time:
                            message += f"📅 发布时间: {publish_time}\n"
                        
                        print(f"DEBUG: Final message length={len(message)}", flush=True)
                        print(f"DEBUG: Sending message to dispatcher...", flush=True)
                        dispatcher.utter_message(text=message)
                        print(f"DEBUG: Message sent!", flush=True)
                    else:
                        dispatcher.utter_message(text="未找到相关资讯，请尝试其他关键词。")
                else:
                    dispatcher.utter_message(text="查询失败，请稍后再试。")
            else:
                dispatcher.utter_message(text="系统暂时不可用，请稍后再试。")
                
        except Exception as e:
            print(f"Error searching news: {e}")
            dispatcher.utter_message(text="抱歉，查询出现错误。")

        return []


class ActionSubmitActivityRegistration(Action):
    """提交活动报名表单"""

    def name(self) -> Text:
        return "action_submit_activity_registration"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        activity_name = tracker.get_slot("activity_name")
        student_id = tracker.get_slot("student_id")
        
        try:
            # 调用后端API提交报名
            response = requests.post(
                f"{API_BASE_URL}/activity/register",
                json={
                    "activityName": activity_name,
                    "studentId": student_id,
                    "userId": tracker.sender_id
                },
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get('code') == 200:
                    dispatcher.utter_message(
                        text=f"✅ 报名成功！\n活动：{activity_name}\n学号：{student_id}\n请注意查收短信通知。"
                    )
                else:
                    dispatcher.utter_message(text=f"报名失败：{data.get('msg', '未知错误')}")
            else:
                dispatcher.utter_message(text="系统繁忙，请稍后再试。")
                
        except Exception as e:
            print(f"Error submitting registration: {e}")
            dispatcher.utter_message(
                text=f"报名信息已记录：\n活动：{activity_name}\n学号：{student_id}\n我们会尽快为您处理。"
            )

        # 清空槽位
        return [AllSlotsReset()]


class ActionDeactivateLoop(Action):
    """停用循环"""

    def name(self) -> Text:
        return "action_deactivate_loop"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(text="好的，已取消操作。")
        return [AllSlotsReset()]


class ValidateActivityRegistrationForm(FormValidationAction):
    """验证活动报名表单"""

    def name(self) -> Text:
        return "validate_activity_registration_form"

    def validate_activity_name(
        self,
        slot_value: Any,
        dispatcher: CollectingDispatcher,
        tracker: Tracker,
        domain: DomainDict,
    ) -> Dict[Text, Any]:
        """验证活动名称"""
        
        if slot_value and len(slot_value) > 0:
            # 可以在这里检查活动是否存在
            return {"activity_name": slot_value}
        else:
            dispatcher.utter_message(text="请输入有效的活动名称")
            return {"activity_name": None}

    def validate_student_id(
        self,
        slot_value: Any,
        dispatcher: CollectingDispatcher,
        tracker: Tracker,
        domain: DomainDict,
    ) -> Dict[Text, Any]:
        """验证学号"""
        
        # 验证学号格式（10位数字）
        if slot_value and re.match(r'^\d{10}$', str(slot_value)):
            return {"student_id": slot_value}
        else:
            dispatcher.utter_message(text="学号格式不正确，请输入10位数字")
            return {"student_id": None}


class ActionExtractEntities(Action):
    """提取实体信息"""

    def name(self) -> Text:
        return "action_extract_entities"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        entities = tracker.latest_message.get("entities", [])
        
        slots_to_set = []
        
        for entity in entities:
            entity_type = entity.get("entity")
            entity_value = entity.get("value")
            
            if entity_type and entity_value:
                slots_to_set.append(SlotSet(entity_type, entity_value))
        
        return slots_to_set

